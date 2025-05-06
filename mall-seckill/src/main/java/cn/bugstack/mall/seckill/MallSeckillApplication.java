package cn.bugstack.mall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合sentinel
 *      1.1、导入依赖：spring-cloud-starter-alibaba-sentinel
 *      1.2、下载sentinel的控制台
 *      1.3、配置sentinel的地址信息
 *      1.4、在控制台调整参数。【默认所有的流控规则保存在内存中，重启失效】
 *
 *
 *  2、每一个服务都要导入actuator，并且配置：management.endpoints.web.exposure.include=*
 *  3、自定义sentinel的流控返回
 *  4、使用sentinel保护feign远程调用，熔断机制
 *      4.1、调用方的熔断保护：feign.sentinel.enabled=true
 *      4.2、调用方手动指定远程的降级策略。远程服务被降级处理，触发我们的熔断回调方法。
 *      4.3、超大流量的时候，必须牺牲一些远程服务。在服务的提供方（远程服务）指定降级策略
 *          提供方是在运行，但是不运行自己的业务逻辑，返回的是默认的降级数据（限流的数据）
 *  5、自定义受保护的资源
 *      1.代码方式：try (Entry entry = SphU.entry("seckillSkus")) {} catch(BlockException e){}
 *      2.注解方式：@SentinelResource(value = "findCurrentSeckillSkusResource",blockHandler = "blockHandler")
 *           blockHandler函数会在原方法被限流/降级/系统保护的时候调用，而 fallback 函数会针对所有类型的异常。
 *           使用fallback首先方法是静态方法，其次需要制定fallbackClass以及fallback
 *       无论使用哪种都要配置默认的返回不然报错，url请求可以设置统一返回WebCallbackManager.setUrlBlockHandle()_
 */
@EnableRedisHttpSession
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
public class MallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSeckillApplication.class, args);
    }

}
