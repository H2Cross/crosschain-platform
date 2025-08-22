package com.tanklab.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CrosConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有请求路径跨域访问
        registry.addMapping("/**")
                // 是否携带Cookie，默认false
                // 允许的请求头类型
                .allowedHeaders("*")
                // 预检请求的缓存时间（单位：秒）
                .maxAge(3600)
                // 允许的请求方法类型
                .allowedMethods("*")
                // 允许哪些域名进行跨域访问
                .allowedOrigins("*");
    }
}
