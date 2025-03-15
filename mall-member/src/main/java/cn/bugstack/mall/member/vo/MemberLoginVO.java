package cn.bugstack.mall.member.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/14 23:59
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class MemberLoginVO implements Serializable {

    private static final long serialVersionUID = 4179585696906671606L;

    private String loginAccount;

    private String password;
}
