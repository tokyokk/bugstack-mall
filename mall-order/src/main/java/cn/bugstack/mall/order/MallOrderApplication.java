package cn.bugstack.mall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用RabbitMQ：
 * 1、引入amqp场景启动器：RabbitAutoConfiguration 就会自动生效
 * 2、给容器中自动配置了
 *      RabbitTemplate、AmqpAdmin、RabbitMessagingTemplate、CachingConnectionFactory
 *      所有的连接信息都是在
 *          @ConfigurationProperties(prefix = "spring.rabbitmq")
 *          public class RabbitProperties{}
 * 3、给配置文件中配置spring.rabbitmq
 * 4、开启@EnableRabbit
 * 5、监听消息：@RabbitListener,必须有@EnableRabbit
 *      @RabbitListener：类+方法上（监听哪些队列）
 *      @RabbitHandler：方法上（重载区分不同的消息）
 */
@EnableRabbit
@SpringBootApplication
public class MallOrderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
