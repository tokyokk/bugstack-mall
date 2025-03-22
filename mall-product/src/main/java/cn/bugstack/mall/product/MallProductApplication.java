package cn.bugstack.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/17 19:59
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@EnableRedisHttpSession // 整合redis作为session存储，高版本可能不需要开启
@EnableFeignClients(basePackages = {"cn.bugstack.mall.product.feign"})
@EnableDiscoveryClient
@MapperScan("cn.bugstack.mall.product.dao")
@SpringBootApplication
public class MallProductApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }
}
