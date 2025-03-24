package cn.bugstack.mall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 22:36
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class OrderConfirmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  收获地址
     */
    @Getter
    @Setter
    private List<MemberAddressVO> address;

    /**
     * 所有选中的购物项
     */
    @Getter
    @Setter
    private List<OrderItemVO> items;

    // 发票信息

    /**
     * 会员积分
     */
    @Getter
    @Setter
    private Integer integration;

    /**
     * 订单防重复提交
     */
    @Getter
    @Setter
    private String orderToken;

    @Getter
    @Setter
    private Map<Long,Boolean> stocks;

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVO item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

    /**
     * 订单总额
     */
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(BigInteger.ZERO);
        if (items != null) {
            for (OrderItemVO item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 应付价格
     */
    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal(BigInteger.ZERO);
        if (items != null) {
            for (OrderItemVO item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }
}
