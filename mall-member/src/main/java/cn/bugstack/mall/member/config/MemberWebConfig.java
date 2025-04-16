package cn.bugstack.mall.member.config;

import cn.bugstack.mall.member.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/17 00:23
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    private final LoginUserInterceptor loginUserInterceptor;

    public MemberWebConfig(LoginUserInterceptor loginUserInterceptor) {
        this.loginUserInterceptor = loginUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
