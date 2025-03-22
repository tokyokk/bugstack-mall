package cn.bugstack.mall.order.config;

import cn.bugstack.mall.order.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 22:22
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {


    private final LoginUserInterceptor loginInterceptor;

    public OrderWebConfiguration(LoginUserInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}
