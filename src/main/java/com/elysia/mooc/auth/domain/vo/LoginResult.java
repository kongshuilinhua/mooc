package com.elysia.mooc.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录成功响应对象。
 */
@Data
@Builder
public class LoginResult {

    /** 访问令牌。 */
    private String accessToken;

    /** 刷新令牌。 */
    private String refreshToken;

    /** 令牌类型。 */
    private String tokenType;

    /** accessToken 有效秒数。 */
    private Long expiresIn;

    /** 当前用户基础信息。 */
    private CurrentUserVO user;
}
