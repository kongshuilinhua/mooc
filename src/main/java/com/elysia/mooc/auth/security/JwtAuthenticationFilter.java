package com.elysia.mooc.auth.security;

import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.util.JwtTokenProvider;
import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.rbac.service.RbacService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 认证过滤器，从 Authorization 请求头中解析当前登录用户并加载角色权限。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RbacService rbacService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            LoginUser loginUser = jwtTokenProvider.parseAccessToken(token);
            // 从数据库加载角色和权限
            Map<String, List<String>> rbac = rbacService.loadUserRolesAndPermissions(loginUser.getUserId());
            List<String> roles = rbac.getOrDefault("roles", List.of());
            List<String> permissions = rbac.getOrDefault("permissions", List.of());
            loginUser.setRoles(roles);
            loginUser.setPermissions(permissions);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role));
            }
            for (String perm : permissions) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    loginUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, AuthErrorCode.AUTH_TOKEN_EXPIRED.message());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResult<Void> result = ApiResult.fail(AuthErrorCode.AUTH_TOKEN_EXPIRED.code(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
