package cn.bugstack.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/3 18:55
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CateLog2VO {

    /**
     * 一级父分类
     */
    private String catalog1Id;
    /**
     * 三级子分类
     */
    private List<Catalog3VO> catalog3List;

    private String id;

    private String name;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catalog3VO {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
