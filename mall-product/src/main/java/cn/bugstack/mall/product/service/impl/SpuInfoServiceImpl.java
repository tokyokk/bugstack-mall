package cn.bugstack.mall.product.service.impl;

import cn.bugstack.common.constant.ProductConstant;
import cn.bugstack.common.to.SkuHasStockVO;
import cn.bugstack.common.to.SkuReductionTO;
import cn.bugstack.common.to.SpuBoundTO;
import cn.bugstack.common.to.es.SkuEsModel;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.product.entity.*;
import cn.bugstack.mall.product.feign.CouponFeignService;
import cn.bugstack.mall.product.feign.SearchFeignService;
import cn.bugstack.mall.product.feign.WareFeignService;
import cn.bugstack.mall.product.service.*;
import cn.bugstack.mall.product.vo.*;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1.保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = SpuInfoEntity.builder()
                .spuName(vo.getSpuName())
                .spuDescription(vo.getSpuDescription())
                .catalogId(vo.getCatalogId())
                .brandId(vo.getBrandId())
                .weight(vo.getWeight())
                .publishStatus(vo.getPublishStatus())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2.保存spu的描述图片 pms_spu_info_desc
        List<String> desc = Optional.ofNullable(vo.getDecript()).orElse(Collections.emptyList());
        SpuInfoDescEntity descEntity = SpuInfoDescEntity.builder()
                .spuId(spuInfoEntity.getId())
                .decript(String.join(",", desc))
                .build();

        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3.保存spu的图片集 pms_spu_images
        List<String> images = Optional.ofNullable(vo.getImages()).orElse(Collections.emptyList());
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4.保存spu的规格参数以及商品属性 pms_product_attr_value
        List<BaseAttrs> baseAttrsList = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrsList.stream().map(attr -> {
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            return ProductAttrValueEntity.builder()
                    .attrId(attr.getAttrId())
                    .attrName(Optional.ofNullable(attrEntity).map(AttrEntity::getAttrName).orElse(""))
                    .attrValue(attr.getAttrValues())
                    .quickShow(attr.getShowDesc())
                    .spuId(spuInfoEntity.getId())
                    .build();
        }).collect(Collectors.toList());
        productAttrValueService.saveproductAttrValue(productAttrValueEntityList);

        // 5.保存spu的积分设置 mall_sms: sms_spu_bounds
        Bounds bounds = Optional.ofNullable(vo.getBounds()).orElse(new Bounds());
        SpuBoundTO spuBoundTo = SpuBoundTO.builder()
                .spuId(spuInfoEntity.getId())
                .buyBounds(bounds.getBuyBounds())
                .growBounds(bounds.getGrowBounds())
                .build();
        R result = couponFeignService.saveSpuBounds(spuBoundTo);
        if (result.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 5.保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        Optional.ofNullable(skus).ifPresent(skuList -> {
            skuList.forEach(sku -> {
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                // 5.1 保存sku基本信息 pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> skuImagesEntities = sku.getImages().stream().map(img ->
                        SkuImagesEntity.builder()
                                .skuId(skuId)
                                .imgUrl(img.getImgUrl())
                                .defaultImg(img.getDefaultImg())
                                .build()
                ).filter(img -> StringUtils.isNotBlank(img.getImgUrl())).collect(Collectors.toList());

                // 5.2 保存sku图片信息 pms_sku_images
                skuImagesService.saveBatch(skuImagesEntities);

                // 5.3 保存sku的销售属性 pms_sku_sale_attr_value
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = sku.getAttr().stream()
                        .map(attr -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuId);
                            return skuSaleAttrValueEntity;
                        }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4 保存sku的优惠、满减等信息 mall_sms: sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTO skuReductionTo = new SkuReductionTO();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(BigInteger.ZERO)) == 1) {
                    R r = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        });

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(item -> {
                item.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotBlank(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catalogId = (String) params.get("catalogId");
        if (StringUtils.isNotBlank(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            queryWrapper.eq("catalog_id", catalogId);
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1.查询当前spuId对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuList = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skuList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 6.查询当前sku所有可以用来被检索的规格属性
        List<ProductAttrValueEntity> attrValueList = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = attrValueList.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        // 在指定的列表中查询可以检索的属性
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = attrValueList.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // 3.查询库存服务是否有库存
        Map<Long, Boolean> skuStockMap = null;
        try {
            R r = wareFeignService.getSkuHasStock(skuIdList);
            TypeReference<List<SkuHasStockVO>> typeReference = new TypeReference<List<SkuHasStockVO>>() {
            };
            skuStockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常，原因：{}", e);
        }

        // 2.封装每个sku的信息，转化为es中需要的格式
        Map<Long, Boolean> finalSkuStockMap = skuStockMap;
        List<SkuEsModel> upProducts = skuList.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // 设置库存信息
            if (finalSkuStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalSkuStockMap.get(sku.getSkuId()));
            }
            // 4.热度评分
            skuEsModel.setHotScore(0L);

            // 5.查询品牌和分类的信息
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());
            // 设置检索属性
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 7.保存到es中 ：mall-search
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            // 远程调用成功
            // 1.修改当前spu状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // todo：重复调用？接口幂等性；重试机制
            log.error("远程保存sku信息失败");

            /**
             * feign调用流程：
             *  1. 构造请求，得到一个请求体，转为json
             *      RequestTemplate requestTemplate = buildTemplateFromArgs.create(configKey, args);
             *  2.发送请求进行执行（执行成功会解码响应数据）
             *      executeAndDecode(template)
             *  3.执行请求会有重试机制
             *      while(true) {
             *          try {
             *              return executeAndDecode(template);
             *          } catch () {
             *              try {
             *                  retryer.continueOrPropagate(e);
             *              } catch () {
             *                  throw ex;
             *              }
             *              continue;
             *          }
             *      }
             */
        }
    }

}