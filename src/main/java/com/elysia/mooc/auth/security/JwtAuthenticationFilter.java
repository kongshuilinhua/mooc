package com.elysia.mooc.auth.security;

import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.util.JwtTokenProvider;
import com.elysia.mooc.common.api.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 认证过滤器，从 Authorization 请求头中解析当前登录用户。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * 解析 JWT 并写入 Spring Security 上下文。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // 1. 先从 Authorization 请求头中提取 Bearer Token。
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. 令牌合法时写入认证上下文，后续控制层和鉴权流程即可读取当前登录用户。
            LoginUser loginUser = jwtTokenProvider.parseAccessToken(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    loginUser,
                    null,
                    Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            // 3. 令牌解析失败时清理上下文并直接返回未登录响应，避免带着脏上下文继续进入业务链路。
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, AuthErrorCode.AUTH_TOKEN_EXPIRED.message());
        }
    }

    /**
     * 从请求头中解析 Bearer Token。
     *
     * @param request HTTP 请求
     * @return accessToken，未携带时返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    /**
     * 输出统一的未授权 JSON 响应。
     *
     * @param response HTTP 响应
     * @param message 错误提示
     * @throws IOException IO 异常
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResult<Void> result = ApiResult.fail(AuthErrorCode.AUTH_TOKEN_EXPIRED.code(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}