package cn.bugstack.mall.search.controller;

import cn.bugstack.common.exception.BizCodeEnum;
import cn.bugstack.common.to.es.SkuEsModel;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 15:36
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSearchSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    /**
     * 商品上架功能
     */
    @PostMapping("product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean status = false;
        try {
            status = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSearchController商品上架错误，{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!status) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
