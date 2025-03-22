package cn.bugstack.mall.member.exception;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/13 23:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class PhoneExistException extends RuntimeException{

    public PhoneExistException() {
        super("手机号已存在");
    }
}
