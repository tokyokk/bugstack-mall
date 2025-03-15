package cn.bugstack.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author micro
 * @description 认证服务
 */
@EnableRedisHttpSession // 整合redis作为session存储，高版本可能不需要开启
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class MallAuthServerApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MallAuthServerApplication.class, args);
    }

}
