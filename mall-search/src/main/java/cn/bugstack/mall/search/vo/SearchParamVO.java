package cn.bugstack.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 13:43
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SearchParamVO {

    /*
    商城检索：检索条件分析
        1.全文检索：skuTitle --> keyword
        2.排序：saleCount (销量)、skuPrice (价格)、hotScore (热度评分)
        3.过滤：hasStock、skuPrice区间、brandId、attrs、catalog3Id
        4.聚合：attrs
     */

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：saleCount_asc/desc、skuPrice_asc/desc、hotScore_asc/desc
     */
    private String sort;

    /**
     * 是否显示有货 0 无库存/1 有库存
     */
    private Integer hasStock;

    /**
     * 价格区间查询 skuPrice=1_500/_500/500_
     */
    private String skuPrice;

    /**
     * 按照品牌进行查询 brandId=1&brandId=2
     */
    private List<Long> brandId;

    /**
     * 按照属性进行查询 attr=1_5寸:8寸&attr=2_16G:8G
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的查询条件
     */
    private String _queryString;
}
