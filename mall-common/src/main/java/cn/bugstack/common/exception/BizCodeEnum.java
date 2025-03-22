package cn.bugstack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/21 22:06
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Getter
@AllArgsConstructor
public enum BizCodeEnum {

    /**
     * 参数列表：
     *  10：通用
     *      001 ：参数格式校验
     *      002 ：短信验证码校验失败
     *  11 ：商品服务
     *  12 ：订单服务
     *  13 ：购物车
     *  14 ：物流
     *  15 ：用户
     */

    // 参数校验失败
    UNKNOWN_EXCEPTION(100000, "系统未知异常"),
    VALID_EXCEPTION(100001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(100002, "验证码已经发送，请稍后再试"),
    PRODUCT_UP_EXCEPTION(110000, "商品上架异常"),
    USER_EXIST_EXCEPTION(150001, "用户已经存在"),
    PHONE_EXIST_EXCEPTION(150002, "手机号已经存在"),
    LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION(150003, "账号或密码错误" );

    private Integer code;
    private String msg;
}
