package com.elysia.mooc.auth.service.impl;

import com.elysia.mooc.auth.constants.AuthErrorCode;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户上下文服务实现。
 */
@Service
public class UserContextServiceImpl implements UserContextService {

    /**
     * 从 Spring Security 上下文获取当前登录用户。
     *
     * @return 当前登录用户
     */
    @Override
    public LoginUser currentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BizException(AuthErrorCode.AUTH_LOGIN_REQUIRED);
        }
        return loginUser;
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 当前用户 ID
     */
    @Override
    public Long currentUserId() {
        return currentLoginUser().getUserId();
    }
}
