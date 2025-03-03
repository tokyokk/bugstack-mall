package cn.bugstack.mall.ware.service.impl;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.ware.feign.ProductFeignService;
import cn.bugstack.mall.ware.vo.SkuHasStockVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.ware.dao.WareSkuDao;
import cn.bugstack.mall.ware.entity.WareSkuEntity;
import cn.bugstack.mall.ware.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

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

}