package cn.bugstack.mall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/24 23:46
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class FareVO {

    private MemberAddressVO address;
    private BigDecimal fare;
}
