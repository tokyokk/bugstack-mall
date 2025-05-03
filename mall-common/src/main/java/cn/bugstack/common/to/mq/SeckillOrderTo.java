package cn.bugstack.common.to.mq;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/5/4 00:28
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SeckillOrderTo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer num;
    /**
     * 会员id
     */
    private Long memberId;
}
