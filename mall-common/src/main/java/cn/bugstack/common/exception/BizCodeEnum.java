package cn.bugstack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/21 22:06
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Getter
@AllArgsConstructor
public enum BizCodeEnum {

    // 参数校验失败
    UNKNOWN_EXCEPTION(100000, "系统未知异常"),
    VALID_EXCEPTION(100001, "参数格式校验失败")
    ;

    private Integer code;
    private String msg;
}
