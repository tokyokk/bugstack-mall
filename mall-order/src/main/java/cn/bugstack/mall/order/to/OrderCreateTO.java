package cn.bugstack.mall.order.to;

import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/26 22:30
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class OrderCreateTO implements Serializable {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice; // 订单应付价格

    private BigDecimal fare; // 运费
}
