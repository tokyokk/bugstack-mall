package cn.bugstack.mall.ware.service.impl;

import cn.bugstack.common.to.mq.StockDetailTO;
import cn.bugstack.common.to.mq.StockLockedTO;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.ware.entity.WareOrderTaskDetailEntity;
import cn.bugstack.mall.ware.entity.WareOrderTaskEntity;
import cn.bugstack.mall.ware.exception.NotStockException;
import cn.bugstack.mall.ware.feign.OrderFeignService;
import cn.bugstack.mall.ware.feign.ProductFeignService;
import cn.bugstack.mall.ware.service.WareOrderTaskDetailService;
import cn.bugstack.mall.ware.service.WareOrderTaskService;
import cn.bugstack.mall.ware.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.ware.dao.WareSkuDao;
import cn.bugstack.mall.ware.entity.WareSkuEntity;
import cn.bugstack.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Slf4j
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private final ProductFeignService productFeignService;
    private final WareSkuDao wareSkuDao;
    private final RabbitTemplate rabbitTemplate;
    private final WareOrderTaskService wareOrderTaskService;
    private final WareOrderTaskDetailService wareOrderTaskDetailService;
    private final OrderFeignService orderFeignService;

    public WareSkuServiceImpl(ProductFeignService productFeignService, WareSkuDao wareSkuDao, RabbitTemplate rabbitTemplate, WareOrderTaskService wareOrderTaskService, WareOrderTaskDetailService wareOrderTaskDetailService, OrderFeignService orderFeignService) {
        this.productFeignService = productFeignService;
        this.wareSkuDao = wareSkuDao;
        this.rabbitTemplate = rabbitTemplate;
        this.wareOrderTaskService = wareOrderTaskService;
        this.wareOrderTaskDetailService = wareOrderTaskDetailService;
        this.orderFeignService = orderFeignService;
    }



    private void unLockStock(Long skuId, Long wareId, Integer num,Long taskDetailId) {
        // 库存解锁
        wareSkuDao.unlockStock(skuId, wareId, num);
        // 更新库存工作单状态
        WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2); // 已解锁
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotBlank(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntityList = baseMapper.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (CollectionUtils.isEmpty(wareSkuEntityList)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程调用商品服务
            try {
                R r = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) r.get("skuInfo");
                if (r.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程调用商品服务失败");
            }
            baseMapper.insert(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVO skuHasStockVO = new SkuHasStockVO();
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVO.setSkuId(skuId);
            skuHasStockVO.setHasStock(count == null ? false : count > 0);
            return skuHasStockVO;
        }).collect(Collectors.toList());
    }

    /**
     * 为某个订单锁定库存
     *  库存解锁的场景：
     *      1.下订单成功，订单过期没有支付被系统自动取消、被用户手动取消。都要解锁库存
     *      2.下订单成功，库存解锁成功，接下来的业务调用失败，导致订单回滚。
     *          之前锁定的库存就要自动解锁。使用seata太慢了
     *
     * @param skuLockVO
     * @return
     */
    @Transactional(rollbackFor = NotStockException.class, readOnly = false)
    @Override
    public Boolean orderLockStock(WareSkuLockVO skuLockVO) {

        /* 保存库存工作单详情
             追溯
        */
        WareOrderTaskEntity wareOrderTask = new WareOrderTaskEntity();
        wareOrderTask.setOrderSn(skuLockVO.getOrderSn());
        wareOrderTaskService.save(wareOrderTask);

        // 1、按照下单的收货地址，找到一个就近仓库，锁定库存。
        // 1、找到每个商品在哪个仓库都有库存
        List<OrderItemVO> locks = skuLockVO.getLocks();
        List<SkuWareHasStock> skuWareHasStockList = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            // 查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            skuWareHasStock.setNum(item.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        Boolean allLock = true;
        for (SkuWareHasStock skuWareHasStock : skuWareHasStockList) {
            Boolean skuStocked = false;
            // 锁定库存
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if (CollectionUtils.isEmpty(wareIds)) {
                // 没有任何仓库有这个商品的库存
                throw new NotStockException(skuId);
            }
            // 1.如果每一个商品都锁定成功，将当前商品锁定了几件都工作党纪录发送给MQ
            // 2.保存失败。前面保存的工作单信息回滚了。发送出去的消息，即使要解锁纪录，由于取数据库查不到指定的id，所以就不用解锁
            for (Long wareId : wareIds) {
                // 成功就返回1，否则返回0
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity(null, skuId, "", skuWareHasStock.getNum(), wareOrderTask.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(
                            entity
                    );
                    // 告诉MQ库存锁定成功
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setId(wareOrderTask.getId());
                    StockDetailTO stockDetailTO = new StockDetailTO();
                    BeanUtils.copyProperties(entity,stockDetailTO);
                    // 只发id不行，防止前面的数据回滚找不到数据
                    stockLockedTO.setDetail(stockDetailTO);
                    
                    rabbitTemplate.convertAndSend("stock-event-exchange","stock-locked",stockLockedTO);
                    break;
                }
            }
            if (!skuStocked) {
                // 当前商品所有库存都没有锁住
                throw new NotStockException(skuId);
            }
        }
        return true;
}

    @Override
    public void unLockStock(StockLockedTO lockedTO) {
        log.info("收到库存解锁的消息");
        StockDetailTO detail = lockedTO.getDetail();
        Long detailId = detail.getId();
        // 解锁
        // 1、查询数据库这个订单的锁定库存信息
        // 有：证明库存锁定成功了
        //      解锁：要看订单情况。
        //。       1.没有这个订单：必须解锁。
        //。       2.有这个订单：要看订单的状态
        //。          1.已取消：解锁库存
        //。          2.没取消：不能解锁
        WareOrderTaskDetailEntity orderTaskDetail = wareOrderTaskDetailService.getById(detailId);
        if (Objects.nonNull(orderTaskDetail)) {
            // 解锁
            Long id = lockedTO.getId(); // 库存工作单的id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            // 根据订单号查询订单状态
            R orderResult = orderFeignService.getOrderStatusByOrderSn(orderSn);
            if (orderResult.getCode() == 0) {
                OrderVO data = orderResult.getData(new TypeReference<OrderVO>() {
                });
                if (data == null || data.getStatus() == 4) {
                    // 订单不存在
                    // 订单已取消，解锁库存
                    if (orderTaskDetail.getLockStatus() == 1) {
                        // 当前库存工作单详情，只有状态为1，已锁定当时未解锁才可以解锁
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                    // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            } else {
                // 消息拒绝，重新翻到队列中，让别人继续消费解锁。
                // channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
                throw new RuntimeException("远程服务调用失败！");
            }
        } else {
            // 无需解锁
            // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    @Data
    class SkuWareHasStock {
    private Long skuId;
    private Integer num;
    private List<Long> wareId;
}

}