package cn.bugstack.mall.order.listener;

import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/8 23:11
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Service
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    private final OrderService orderService;

    public OrderCloseListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @SneakyThrows
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) {
        System.out.println("收到过期的订单信息，准备关闭订单：" + orderEntity.getOrderSn());
        try {
            orderService.closeOrder(orderEntity);
            // 手动调用支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单关闭失败：{}",e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
