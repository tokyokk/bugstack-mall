package cn.bugstack.mall.product.web;

import cn.bugstack.mall.product.service.SkuInfoService;
import cn.bugstack.mall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author micro, 微信：yykk、
 * @description 商品详情
 * @date 2025/3/6 11:35
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) {
        SkuItemVO itemVO =  skuInfoService.getSkuItem(skuId);
        model.addAttribute("item", itemVO);
        return "item";
    }
}
