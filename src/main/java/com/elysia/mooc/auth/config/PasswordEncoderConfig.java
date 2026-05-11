package com.elysia.mooc.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置，统一使用 BCrypt 保存密码密文。
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 注册密码加密器。
     *
     * @return BCrypt 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
