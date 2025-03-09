package cn.bugstack.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author micro
 * @description 认证服务
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class MallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAuthServerApplication.class, args);
    }

}
