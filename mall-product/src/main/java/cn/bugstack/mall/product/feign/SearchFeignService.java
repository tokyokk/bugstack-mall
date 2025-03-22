package cn.bugstack.mall.product.feign;

import cn.bugstack.common.to.es.SkuEsModel;
import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 15:58
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-search")
public interface SearchFeignService {

    /**
     * 商品上架功能
     */
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
