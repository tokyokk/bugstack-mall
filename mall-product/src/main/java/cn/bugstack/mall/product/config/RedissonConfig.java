package cn.bugstack.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/4 13:48
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 单机模式
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        //config.useClusterServers().addNodeAddress("127.0.0.1:7001", "127.0.0.1:7002", "127.0.0.1:7003", "127.0.0.1:7004", "127.0.0.1:7005", "127.0.0.1:7006"); // 集群模式
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
