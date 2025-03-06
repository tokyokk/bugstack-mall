package cn.bugstack.mall.search.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 19:59
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    /**
     * 根据属性ID获取属性信息
     *
     * @param attrId 属性ID
     * @return 属性信息
     */
    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);


    /**
     * 根据品牌ID获取品牌信息
     *
     * @param brandIds 品牌ID集合
     * @return 品牌信息
     */
    @RequestMapping("/product/brand/infos")
    public R brandInfo(@RequestParam("brandIds") List<Long> brandIds);

    /**
     * 根据分类ID获取分类信息
     *
     * @param catId 分类ID
     * @return 分类信息
     */
    @RequestMapping("/product/category/info/{catId}")
    public R categoryInfo(@PathVariable("catId") Long catId);
}
