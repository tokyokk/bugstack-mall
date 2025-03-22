package cn.bugstack.mall.mallcart.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 18:32
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class UserInfoTO implements Serializable {

    private static final long serialVersionUID = 2754830199259181587L;

    private Long userId;

    private String userKey;

    private boolean tempUser = false;
}
