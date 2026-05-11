package com.elysia.mooc.auth.controller;

import com.elysia.mooc.auth.domain.dto.LoginRequest;
import com.elysia.mooc.auth.domain.dto.LogoutRequest;
import com.elysia.mooc.auth.domain.dto.RefreshTokenRequest;
import com.elysia.mooc.auth.domain.dto.RegisterRequest;
import com.elysia.mooc.auth.domain.vo.CurrentUserVO;
import com.elysia.mooc.auth.domain.vo.LoginResult;
import com.elysia.mooc.auth.domain.vo.TokenPairVO;
import com.elysia.mooc.auth.service.AuthService;
import com.elysia.mooc.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口控制器，提供注册、登录、刷新 Token 和退出登录接口。
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 新用户基础信息
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResult<CurrentUserVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResult.ok(authService.register(request));
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @param servletRequest HTTP 请求
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResult<LoginResult> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        return ApiResult.ok(authService.login(request, clientIp(servletRequest)));
    }

    /**
     * 刷新 Token。
     *
     * @param request 刷新请求
     * @return 新 Token
     */
    @Operation(summary = "刷新 Token")
    @PostMapping("/tokens/refresh")
    public ApiResult<TokenPairVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResult.ok(authService.refresh(request));
    }

    /**
     * 退出登录。
     *
     * @param request 退出登录请求
     * @return 是否处理成功
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResult<Boolean> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResult.ok(true);
    }

    /**
     * 解析客户端真实 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    private String clientIp(HttpServletRequest request) {
        // 优先读取反向代理透传的真实客户端地址，兼容 Nginx 或网关转发场景。
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}