package cn.bugstack.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author micro
 * @description 认证服务
 *
 * 核心原理：
 *  1、@EnableRedisHttpSession，导入了RedisHttpSessionConfiguration
 *      1.1 给容器中添加了一个组件
 *          SessionRepository-->RedisOperationsSessionRepository 新版为：RedisIndexedSessionRepository，-->redis操作session的增删改查的封装类
 *      1.2 SessionRepositoryFilter-->Filter：session存储过滤器,每个请求过来都必须经过Filter
 *          1.2.1 创建的时候，自动获取SessionRepository
 *          1.2.2 原生的request，response都被包装SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
 *          1.2.3 以后获取session，request.getSession()=》SessionRepositoryRequestWrapper.getSession()
 *          1.2.3 wrapperRequest.getSession() -->SessionRepository中获取的。
 *   2、装饰器模式
 *
 *   自动续期：redis中的数据也是有过期时间的
 *
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
