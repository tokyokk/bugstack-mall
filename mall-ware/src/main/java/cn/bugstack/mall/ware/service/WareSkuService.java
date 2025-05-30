package cn.bugstack.mall.ware.service;

import cn.bugstack.common.to.OrderTO;
import cn.bugstack.common.to.mq.StockLockedTo;
import cn.bugstack.mall.ware.vo.SkuHasStockVO;
import cn.bugstack.mall.ware.vo.WareSkuLockVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:35:20
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVO skuLockVO);

    void unLockStock(StockLockedTo lockedTO);

    void unLockStock(OrderTO orderTo);
}

