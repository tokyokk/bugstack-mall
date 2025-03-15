package cn.bugstack.mall.auth.vo;

import lombok.Data;
import org.checkerframework.checker.regex.qual.Regex;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/13 22:15
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class UserRegisterVO {

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6, max = 18, message = "用户名长度在6-18之间")
    private String userName;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度在6-18之间")
    private String password;
    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^1[3|4|5|6|7|8|9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码不能为空")
    private String code;
}
