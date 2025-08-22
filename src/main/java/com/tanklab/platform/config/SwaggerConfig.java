package com.tanklab.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Description Swagger工具配置类
 * @Author Zhang Qihang
 * @Date 2021/9/19 14:45
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private String swaggerHost;
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host("60.215.128.50:42424")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tanklab"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo(){

        return new ApiInfoBuilder()
                .title("跨链数据中台")
                .description("API Designed by zzy")
                .version("2.0")
                .contact(new Contact("zzy","","1978750368@qq.com"))
                .build();
    }
}