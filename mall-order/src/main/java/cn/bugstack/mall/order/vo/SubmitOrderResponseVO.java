package cn.bugstack.mall.order.vo;

import cn.bugstack.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/25 23:44
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SubmitOrderResponseVO {

    private OrderEntity order;
    private Integer code; // 0 成功
}
