package cn.bugstack.mall.mallcart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description 购物车
 * @date 2025/3/15 18:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class Cart {

    @Getter
    @Setter
    private List<CartItem> items;

    /**
     * 购物车商品数量
     */
    private Integer countNum;

    /**
     * 购物车商品类型数量
     */
    private Integer countType;

    /**
     * 购物车商品总价
     */
    @Setter
    private BigDecimal totalAmount;

    /**
     * 购物车商品优惠总价
     */
    @Getter
    private BigDecimal reduce = new BigDecimal("0");

    public Integer getCountNum() {
        int count  = 0;
        if (null != items && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count  = 0;
        if (null != items && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 1，计算购物项总价
        for (CartItem item : items) {
            if (Boolean.TRUE.equals(item.getCheck())) {
                BigDecimal itemTotalPrice = item.getTotalPrice();
                amount = amount.add(itemTotalPrice);
            }
        }

        // 2，减去优惠总价
        return amount.subtract(getReduce());
    }

}
