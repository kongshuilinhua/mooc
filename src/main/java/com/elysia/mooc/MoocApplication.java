package com.elysia.mooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MOOC 后端应用启动入口。
 */
@SpringBootApplication
public class MoocApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MoocApplication.class, args);
    }
}
