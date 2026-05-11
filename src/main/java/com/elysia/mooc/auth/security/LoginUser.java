package com.elysia.mooc.auth.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息，保存到 Spring Security 上下文中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户 ID。 */
    private Long userId;

    /** 登录用户名。 */
    private String username;
}
