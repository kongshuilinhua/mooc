package com.elysia.mooc.auth.service;

import com.elysia.mooc.auth.security.LoginUser;

/**
 * 当前登录用户上下文服务。
 */
public interface UserContextService {

    /**
     * 获取当前登录用户。
     *
     * @return 当前登录用户
     */
    LoginUser currentLoginUser();

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    Long currentUserId();
}
