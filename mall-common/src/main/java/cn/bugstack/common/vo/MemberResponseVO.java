package cn.bugstack.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 11:18
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@ToString
@Data
public class MemberResponseVO implements Serializable {

    private static final long serialVersionUID = -1073816803481407530L;

    private Long id;
    /**
     * 会员等级id
     */
    private Long levelId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String header;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 生日
     */
    private Date birth;
    /**
     * 所在城市
     */
    private String city;
    /**
     * 职业
     */
    private String job;
    /**
     * 个性签名
     */
    private String sign;
    /**
     * 用户来源
     */
    private Integer sourceType;
    /**
     * 积分
     */
    private Integer integration;
    /**
     * 成长值
     */
    private Integer growth;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 注册时间
     */
    private Date createTime;

    private String socialUid;
    private String accessToken;
    private String expiresIn;
}
