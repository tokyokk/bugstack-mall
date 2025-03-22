package cn.bugstack.mall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/13 22:15
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class MemberRegisterVO {

    private String userName;
    private String password;
    private String phone;
}
