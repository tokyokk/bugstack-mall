package cn.bugstack.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/20 20:32
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Getter
@AllArgsConstructor
public enum FeignCodeEnum {

    /**
     * 关于feign的状态码枚举
     */
    SUCCESS(0, "成功");

    private final int code;
    private final String message;
}
