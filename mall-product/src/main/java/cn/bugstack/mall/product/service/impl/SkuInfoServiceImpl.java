package cn.bugstack.mall.product.service.impl;

import cn.bugstack.mall.product.entity.SkuImagesEntity;
import cn.bugstack.mall.product.entity.SpuInfoDescEntity;
import cn.bugstack.mall.product.service.*;
import cn.bugstack.mall.product.vo.SkuItemSaleAttrVO;
import cn.bugstack.mall.product.vo.SkuItemVO;
import cn.bugstack.mall.product.vo.SpuItemAttrGroupVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.SkuInfoDao;
import cn.bugstack.mall.product.entity.SkuInfoEntity;


@Slf4j
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(item -> {
                item.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotBlank(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catalogId = (String) params.get("catalogId");
        if (StringUtils.isNotBlank(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }

        String min = (String) params.get("min");
        if (StringUtils.isNotBlank(min)) {
            queryWrapper.ge("price", min);
        }

        String max = (String) params.get("max");
        if (StringUtils.isNotBlank(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                log.error("max值不正确");
            }
        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        return this.list(Wrappers.<SkuInfoEntity>lambdaQuery().eq(SkuInfoEntity::getSpuId, spuId));
    }

    @Override
    public SkuItemVO getSkuItem(Long skuId) {
        SkuItemVO itemVO = new SkuItemVO();
        // 1. sku基本信息获取
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = baseMapper.selectById(skuId);
            itemVO.setInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            // 3. 获取spu的销售属性组合
            List<SkuItemSaleAttrVO> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(skuInfo.getSpuId());
            itemVO.setSaleAttr(saleAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            // 4. 获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfo.getSpuId());
            itemVO.setDesc(spuInfoDescEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            // 5. 获取spu的规格参数信息
            List<SpuItemAttrGroupVO> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfo.getSpuId(), skuInfo.getCatalogId());
            itemVO.setGroupAttrs(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2. sku图片信息获取
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            itemVO.setImages(images);
        }, threadPoolExecutor);

        try {
            CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取sku详情异常：{}", e);

        }

        return itemVO;
    }

}