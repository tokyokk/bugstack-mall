package cn.bugstack.mall.order.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/26 23:25
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    /**
     * 根据skuId查询spu信息
     */
    @GetMapping("/product/spuinfo/{skuId}/spuinfo")
    public R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
