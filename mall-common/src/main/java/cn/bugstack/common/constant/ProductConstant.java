package cn.bugstack.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/23 22:42
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class ProductConstant {

    @Getter
    @AllArgsConstructor
    public enum AttrEnum {

        /**
         * 基本属性
         */
        ATTR_TYPE_BASE(1, "基本属性"),
        /**
         * 销售属性
         */
        ATTR_TYPE_SALE(0, "销售属性");

        private final int code;
        private final String msg;
    }

    @Getter
    @AllArgsConstructor
    public enum StatusEnum {

        /**
         * 新建
         */
        NEW_SPU(0, ""),
        /**
         * 商品上架
         */
        SPU_UP(1, "商品上架"),
        /**
         * 商品下架
         */
        SPU_DOWN(2, "商品下架");

        private final int code;
        private final String msg;
    }
}
