package cn.bugstack.mall.order.controller;

import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 15:16
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(final @RequestParam(value = "num", required = false, defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {
                final OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setName("测试");
                orderReturnReasonEntity.setCreateTime(new Date());
                orderReturnReasonEntity.setSort(0);
                orderReturnReasonEntity.setStatus(1);
                rabbitTemplate.convertAndSend("order.direct.exchange", "order.direct.routing.key", orderReturnReasonEntity,
                        new CorrelationData(System.currentTimeMillis() + ""));
            } else {
                final OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn("1234567890");
                rabbitTemplate.convertAndSend("order.direct.exchange", "order.direct.routing.key", orderEntity,
                        new CorrelationData(System.currentTimeMillis() + ""));
            }

        }
        return "ok";
    }
}
