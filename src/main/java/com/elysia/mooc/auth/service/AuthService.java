package com.elysia.mooc.auth.service;

import com.elysia.mooc.auth.domain.dto.LoginRequest;
import com.elysia.mooc.auth.domain.dto.LogoutRequest;
import com.elysia.mooc.auth.domain.dto.RefreshTokenRequest;
import com.elysia.mooc.auth.domain.dto.RegisterRequest;
import com.elysia.mooc.auth.domain.vo.CurrentUserVO;
import com.elysia.mooc.auth.domain.vo.LoginResult;
import com.elysia.mooc.auth.domain.vo.TokenPairVO;

/**
 * 认证业务服务。
 */
public interface AuthService {

    /**
     * 注册用户。
     *
     * @param request 注册请求
     * @return 新用户基础信息
     */
    CurrentUserVO register(RegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @param clientIp 客户端 IP
     * @return 登录结果
     */
    LoginResult login(LoginRequest request, String clientIp);

    /**
     * 刷新 Token。
     *
     * @param request 刷新 Token 请求
     * @return 新 Token
     */
    TokenPairVO refresh(RefreshTokenRequest request);

    /**
     * 退出登录。
     *
     * @param request 退出登录请求
     */
    void logout(LogoutRequest request);

    /**
     * 查询当前登录用户。
     *
     * @return 当前用户信息
     */
    CurrentUserVO currentUser();
}
