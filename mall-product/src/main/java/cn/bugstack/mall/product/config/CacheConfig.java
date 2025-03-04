package cn.bugstack.mall.product.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/4 16:52
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 *
 *
 * Spring-Cache不足：
 *  1）读模式：
 *      1.缓存穿透：查询一个null值。解决：spring.cache.redis.null-values=true
 *      2.缓存击穿：大量并发请求过来刚好查询一个正好过期的数据，解决：加锁   默认无加锁：sync=true（加锁：解决击穿）
 *      3.缓存雪崩：大量的key同时过期。解决：加过期时间，加上过期时间 spring.cache.redis.time-to-live=3600000
 *  2）写模式：（缓存与数据库一致）
 *      1.读写加锁。 适用于读多写少场景。
 *      2.引入Cancel来感知MYSQL的更新然后去更新数据库。
 *      3.读多写多，直接去数据库查询就行。
 *   总结：
 *      常规数据：（读多写少，一致性，及时性不高的数据）可以使用spring-cache，缓存的数据只要有过期时间就够了
 *      特殊数据：特殊处理，引入中间件等处理！
 *   原理：
 *      CacheManager（RedisCacheManager） ——> Cache （RedisCache）缓存组件 --> Cache负责缓存的读写
 */
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
@Configuration
public class CacheConfig {


    /**
     * 配置后，发现配置文件中的配置失效了：
     *  1.原来的配置文件是这样的：
     *      @ConfigurationProperties(prefix = "spring.cache")
     *      public class CacheProperties{}
     *  2.想要生效：
     *      @EnableConfigurationProperties(CacheProperties.class)
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) { // 这里方法传的所有参数都会从容器中确定，所以这样传递
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();

        configuration = configuration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        // 设置让配置文件中的配置生效
        if (redisProperties.getTimeToLive() != null) {
            configuration = configuration.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            configuration = configuration.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }

        return configuration;

    }

}
