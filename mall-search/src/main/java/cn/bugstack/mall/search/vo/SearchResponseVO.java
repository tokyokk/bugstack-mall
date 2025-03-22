package cn.bugstack.mall.search.vo;

import cn.bugstack.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/5 14:03
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SearchResponseVO {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;
    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 可以遍历的页码
     */
    private List<Integer> pageNavs;

    /**
     * 当前查询到的结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询到的结果，所有涉及到的属性
     */
    private List<AttrVo> attrs;

    /**
     * 当前查询到的结果，所有涉及到的分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 哪些attrId 被选中了
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}
