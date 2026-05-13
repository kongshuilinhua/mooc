package com.elysia.mooc.config;

import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.security.JwtAuthenticationFilter;
import com.elysia.mooc.common.api.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置，使用 JWT 保护登录态接口。
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        /**
         * 配置安全过滤链。
         *
         * @param http                    HttpSecurity 配置对象
         * @param jwtAuthenticationFilter JWT 认证过滤器
         * @param objectMapper            JSON 序列化工具
         * @return 安全过滤链
         * @throws Exception Spring Security 配置异常
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        ObjectMapper objectMapper) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                // 使用无状态会话，登录态完全依赖 JWT 传递。
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                // 先放行健康检查、文档和认证入口，其余接口统一要求登录。
                                                .requestMatchers(
                                                                "/api/ping",
                                                                "/actuator/health",
                                                                "/actuator/info",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api/auth/register",
                                                                "/api/auth/login",
                                                                "/api/auth/tokens/refresh")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/courses",
                                                                "/api/courses/*",
                                                                "/api/courses/*/catalog",
                                                                "/api/course-categories",
                                                                "/api/course-tags")
                                                .permitAll()
                                                // 管理端接口先在过滤链做角色拦截，确保普通学生返回 HTTP 403。
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response,
                                                                authException) -> writeJson(response, objectMapper,
                                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                                ApiResult.fail(AuthErrorCode.AUTH_LOGIN_REQUIRED
                                                                                                .code(), "请先登录")))
                                                .accessDeniedHandler(
                                                                (request, response, accessDeniedException) -> writeJson(
                                                                                response, objectMapper,
                                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                                ApiResult.fail(403, "没有权限访问该资源"))))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable);

                return http.build();
        }

        /**
         * 配置本地前端开发环境跨域规则。
         *
         * @return CORS 配置源
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                                "http://127.0.0.1:5173",
                                "http://localhost:5173",
                                "http://127.0.0.1:5174",
                                "http://localhost:5174"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        /**
         * 输出统一的 JSON 响应体。
         *
         * @param response     HTTP 响应
         * @param objectMapper JSON 序列化工具
         * @param httpStatus   HTTP 状态码
         * @param result       统一响应对象
         * @throws IOException IO 异常
         */
        private void writeJson(
                        HttpServletResponse response,
                        ObjectMapper objectMapper,
                        int httpStatus,
                        ApiResult<?> result) throws IOException {
                response.setStatus(httpStatus);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(result));
        }
}
