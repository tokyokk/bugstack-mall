package cn.bugstack.mall.search.service;

import cn.bugstack.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 15:38
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public interface ProductSaveService {
    /**
     * @param skuEsModels 商品数据
     * @return
     */
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
