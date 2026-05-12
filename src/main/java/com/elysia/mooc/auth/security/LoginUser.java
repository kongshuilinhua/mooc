package com.elysia.mooc.auth.security;

import java.util.Collections;
import java.util.List;
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

    /** 用户 ID */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** 角色编码列表 */
    private List<String> roles;

    /** 权限编码列表 */
    private List<String> permissions;

    public LoginUser(Long userId, String username) {
        this(userId, username, Collections.emptyList(), Collections.emptyList());
    }
}
