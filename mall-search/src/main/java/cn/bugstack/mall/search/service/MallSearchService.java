package cn.bugstack.mall.search.service;

import cn.bugstack.mall.search.vo.SearchParamVO;
import cn.bugstack.mall.search.vo.SearchResponseVO;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 13:44
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public interface MallSearchService {

    /**
     * 商品搜索
     * @param searchParam 检索参数
     * @return 检索结果
     */
    SearchResponseVO search(SearchParamVO searchParam);
}
