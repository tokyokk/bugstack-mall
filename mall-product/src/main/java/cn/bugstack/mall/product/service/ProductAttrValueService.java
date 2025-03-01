package cn.bugstack.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveproductAttrValue(List<ProductAttrValueEntity> productAttrValueEntityList);

    List<ProductAttrValueEntity> baseAttrListForSpu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> productAttrValueEntityList);
}

