package cn.bugstack.mall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

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
 *      @RabbitHandler：方法上（重载区分不同的消息） 本地事务失效问题
 * 同一个对象内事务方法互调默认失效，原因 绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 *      1）、引入aop-starterjspring-boot-starter-aop；引入了aspectj
 *      2）、@EnabLeAspectJAutoProxy；开启 aspectj 动态代理功能。以后所有的动态代理都是aspectj创建的（即使没有借口也可以创建动态代理）
 *              对外暴露代理对象
 *      3)、本类互调用代理对象调用:
 *          OrderService  o = (OrderService) AopContext.currentProxy(); 强制转换为自己的借口或者实现类
 *
 *    Seata控制分布式事务：
 *      1）、引入seata的依赖
 *      2）、安装事物协调器：seata-server：https://github.com/apache/incubator-seata/releases
 *      3）、整合步骤
 *          1）、引入seata的依赖：spring-cloud-starter-alibaba-seata seata-all-0.7.1
 *          2)、启动seata-server
 *              registry.conf:注册中心配置：修改registry type=nacos
 *              file.conf:
 *          3）、全局事务：@GlobalTransaction，每一个远程的小事务使用@Transaction
 *          4）、所有想要用到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 *          5）、每一个微服务导入
 *              file.conf：vgroup_mapping.{application.name}-fescar-service-group = ‘default’
 *              registry.conf
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class MallOrderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
