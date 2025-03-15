package cn.bugstack.mall.auth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/14 23:36
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 8227422802577726132L;

    private String loginAccount;

    private String password;
}
