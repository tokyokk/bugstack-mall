package cn.bugstack.mall.product.feign;

import cn.bugstack.common.to.SkuHasStockVO;
import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 15:19
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-ware")
public interface WareFeignService {

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
