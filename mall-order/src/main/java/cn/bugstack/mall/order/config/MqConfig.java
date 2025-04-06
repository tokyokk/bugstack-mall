package cn.bugstack.mall.order.config;

import cn.bugstack.mall.order.entity.OrderEntity;
import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/3 21:16
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Configuration
public class MqConfig {

    @SneakyThrows
    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity orderEntity, Channel channel, Message message) {
        System.out.println("收到过期的订单信息，准备关闭订单：" + orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

    /**
     * 容器中的 Queue Exchange Binding都会自动创建，（RabbitMQ没有的情况下）
     * RabbitMQ中只要有，@Bean声明的属性发生变化也不会覆盖。
     */
    @Bean
    public Queue orderDelayQueue() {
        /*
          延迟队列的参数
          x-dead-letter-exchange：代表消息过期后转发的交换机
          x-dead-letter-routing-key：代表消息过期后转发的路由键
          x-message-ttl：代表消息的过期时间，单位毫秒
         */
        final Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("x-dead-letter-exchange", "order.event.exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);

        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false, null);
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false, null);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.order", null);
    }
}
