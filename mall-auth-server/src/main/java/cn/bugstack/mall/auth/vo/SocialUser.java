package cn.bugstack.mall.auth.vo;

import lombok.Data;

import java.io.Serializable;


@Data
public class SocialUser implements Serializable {

    private static final long serialVersionUID = 7648665129818157320L;

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;

}
