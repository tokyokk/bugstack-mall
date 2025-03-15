package cn.bugstack.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 12:07
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Configuration
public class MallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        final DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("mall.com");
        cookieSerializer.setCookieName("MALL_SESSION");
        return cookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
