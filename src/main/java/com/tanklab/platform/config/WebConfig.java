package com.tanklab.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，并拦截所有请求（可根据需要调整路径，如 /api/**）
        registry.addInterceptor(new TimeLogInterceptor())
                .addPathPatterns("/platform/**") // 拦截的路径
                .excludePathPatterns("/static/**"); // 排除静态资源（可选）
    }
}