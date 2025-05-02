package cn.bugstack.mall.product.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/5/1 00:15
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient(name = "mall-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId);
}
