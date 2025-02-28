package cn.bugstack.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

