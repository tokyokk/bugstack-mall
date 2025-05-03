package cn.bugstack.mall.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 14:44
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制化RabbitTemplate
     * 发送端：
     *      1、服务器收到消息就回调：
     *          1.spring.rabbitmq.publisher-confirms=true
     *          2.设置确认回调ConfirmCallback
     *      2、消息正确抵达队列进行回调：
     *          1.spring.rabbitmq.publisher-returns=true
     *            spring.rabbitmq.template.mandatory=true
     *
     *  消费端：
     *      3.消费端确认（保证每一个消息被正确消费，此时broker才会删除这个消息）
     *          1.默认是自动确认的，只要消息接收到，服务端就会移除这个消息
     *              问题：
     *                  我们收到很多消息，自动回复给服务器ack，但是只有一个消息处理成功，宕机了，发生消息丢失！
     *                  ✅消费者手动给确认模式：只要我们没有明确告诉MQ消息被签收，消息就会一直处于unacked状态，即使Consumer宕机，消息不会丢失，会重新变为ready状态。再次启动的话讲ready再次变更为unacked状态进行处理！
     *          2.如何签收：
     *              channel.basicAck(messageProperties.getDeliveryTag(), false);签收获取
     *              channel.basicNack(messageProperties.getDeliveryTag(), false, true); negatively ack， negatively ack=true，重新入队列， negatively ack=false，丢弃消息。
     *              channel.basicReject(messageProperties.getDeliveryTag(), true); negatively ack， negatively ack=true，重新入队列， negatively ack=false，丢弃消息。
     */
    @Bean
    @PostConstruct // 在RabbitConfig对象创建完成之后，执行这个方法
    public void initRabbitTemplate() {
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 1.只要消息抵达Broker 就ack=true
             *
             * @param correlationData 当前消息的关联数据（这个是消息的唯一id）
             * @param ack             是否成功收到
             * @param cause           失败的原因
             */
            @Override
            public void confirm(final CorrelationData correlationData, final boolean ack, final String cause) {
                // 服务器收到了
                /*
                * 1、做好消息确认机制（publisher，consumer【手动ack】）
                * 2、每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送
                * */
                // 修改消息的状态
                if (ack) {
                    log.info("【消息发送成功】---> {}", correlationData.getId());
                } else {
                    log.info("【消息发送失败】---> {}", cause);
                }
            }
        });
        // 设置消息抵达队列的消息回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             *
             * @param message    投递失败的消息详细信息
             * @param replyCode  回复的消息
             * @param replyText  回复的文本内容
             * @param exchange   这个消息发给哪个交换机
             * @param routingKey 消息的路由键
             */
            @Override
            public void returnedMessage(final Message message, final int replyCode, final String replyText, final String exchange, final String routingKey) {
                // 报错误了，修改数据库当前消息的状态--》错误
                log.info("消息 {} 投递失败，应答码：{} 原因：{} 交换机: {} 路由键: {}", message, replyCode, replyText, exchange, routingKey);
            }
        });

        // 消息序列化
        rabbitTemplate.setMessageConverter(messageConverter());
    }
}
