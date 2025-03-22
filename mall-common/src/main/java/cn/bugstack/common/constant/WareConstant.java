package cn.bugstack.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/23 22:42
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class WareConstant {

    @Getter
    @AllArgsConstructor
    public enum PurchaseStatusEnum {

        /**
         * 新建状态
         */
        CREATED(0, "新建"),
        /**
         * 已分配
         */
        ASSIGNED(1, "已分配"),
        /**
         * 已领取
         */
        RECEIVED(2, "已领取"),
        /**
         * 已完成
         */
        FINISH(3, "已完成"),
        /**
         * 异常
         */
        HASERROR(4, "有异常");

        private final int code;
        private final String msg;

    }

    @Getter
    @AllArgsConstructor
    public enum PurchaseDetailsEnum {

        /**
         * 新建状态
         */
        CREATED(0, "新建"),
        /**
         * 已分配
         */
        ASSIGNED(1, "已分配"),
        /**
         * 正在采购
         */
        BUYING(2, "正在采购"),
        /**
         * 已完成
         */
        FINISH(3, "已完成"),
        /**
         * 采购失败
         */
        FAIL(4, "采购失败");

        private final int code;
        private final String msg;

    }
}
