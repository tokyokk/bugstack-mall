package cn.bugstack.mall.product.vo;

import cn.bugstack.mall.product.entity.SkuImagesEntity;
import cn.bugstack.mall.product.entity.SkuInfoEntity;
import cn.bugstack.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/6 11:44
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SkuItemVO {

    /**
     * 1. sku基本信息获取
     */
    private SkuInfoEntity info;

    /**
     * 2. sku图片信息获取
     */
    private List<SkuImagesEntity> images;

    /**
     * 3. 获取spu的销售属性组合
     */
    private List<SkuItemSaleAttrVO> saleAttr;

    /**
     * 4. 获取spu的介绍
     */
    private SpuInfoDescEntity desc;

    /**
     * 5. 获取spu的规格参数信息
     */
    private List<SpuItemAttrGroupVO> groupAttrs;

    /**
     * 是否有货/无货
     */
    private boolean hasStock = true;

}
