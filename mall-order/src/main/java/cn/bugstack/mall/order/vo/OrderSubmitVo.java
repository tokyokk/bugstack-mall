package cn.bugstack.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/25 23:30
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class OrderSubmitVo {

    /**
     * 收货地址id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 无需提交购买的商品，去购物车在获取一遍
     * 优惠，发票
     *
     */

    /**
     * 防重令牌
     */
    private String orderToken;

    /**
     * 应付价格：验价
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;

    // 用户相关信息去session取
}
