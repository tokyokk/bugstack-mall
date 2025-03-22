package cn.bugstack.mall.order;

import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.entity.OrderReturnReasonEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
class MallOrderApplicationTests {

    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessageTest() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                final OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setName("测试");
                orderReturnReasonEntity.setCreateTime(new Date());
                orderReturnReasonEntity.setSort(0);
                orderReturnReasonEntity.setStatus(1);
                rabbitTemplate.convertAndSend("order.direct.exchange", "order.direct.routing.key", orderReturnReasonEntity);
            } else {
                final OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn("1234567890");
                rabbitTemplate.convertAndSend("order.direct.exchange", "order.direct.routing.key", orderEntity);
            }

        }

        // 1.发送消息,如果发送的消息是个对象，那么会使用序列化机制,将对象写出去,所以比需实现Serializable
        // rabbitTemplate.convertAndSend("order.direct.exchange", "order.direct.routing.key", "Hello World");
        // 2.发送消息的类型，可以是一个json
        log.info("【发送消息】---> {}", "Hello World");
    }

    /**
     * 1、如何创建Exchange、Queue、Binding
     *      1.1 使用AmqpAdmin创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        /**
         * 创建交换机Exchange：
         * 参数解释：交换机的名字，是否持久化，是否自动删除，参数
         * public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {}
         */
        final DirectExchange directExchange = new DirectExchange("order.direct.exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("【创建交换机】---> {}", directExchange.getName());
    }

    @Test
    public void createQueue() {
        /**
         * 创建队列Queue：
         * 参数解释：队列的名字，是否持久化，是否自动删除，是否排他，参数
         * public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {}
         */
        final Queue queue = new Queue("order.direct.queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("【创建队列】---> {}", "order.direct.queue");
    }

    @Test
    public void createBinding() {
        // 创建绑定关系
        /**
         *  参数解释：目的地,目的地类型,交换机，路由键,自定义参数
         * 	public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
         * 			            @Nullable Map<String, Object> arguments) {}
         */
        final Binding binding = new Binding("order.direct.queue", Binding.DestinationType.QUEUE, "order.direct.exchange", "order.direct.routing.key", null);
        amqpAdmin.declareBinding(binding);
        log.info("【创建绑定关系】---> {}", "order.direct.binding");
    }

}
