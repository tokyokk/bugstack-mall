package cn.bugstack.mall.order.listener;

import cn.bugstack.common.to.mq.SeckillOrderTo;
import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/5/4 00:39
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Component
@Slf4j
public class OrderSeckillListener {

    private final OrderService orderService;

    public OrderSeckillListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @SneakyThrows
    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) {

        try {
            log.info("准备创建秒杀单的详细信息");
            orderService.createSeckillOrder(seckillOrderTo);
            // 手动调用支付宝收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单关闭失败：{}",e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
