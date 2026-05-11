package com.elysia.mooc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置，统一定义 Swagger 页面展示的基础信息。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 创建 MOOC 后端 OpenAPI 文档配置。
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI moocOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MOOC API")
                        .version("1.0.0")
                        .description("MOOC 后端接口文档"));
    }
}
