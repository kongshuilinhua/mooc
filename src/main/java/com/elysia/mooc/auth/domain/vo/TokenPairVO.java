package com.elysia.mooc.auth.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * Token 刷新响应对象。
 */
@Data
@Builder
public class TokenPairVO {

    /** 新访问令牌。 */
    private String accessToken;

    /** 新刷新令牌。 */
    private String refreshToken;

    /** 令牌类型。 */
    private String tokenType;

    /** accessToken 有效秒数。 */
    private Long expiresIn;
}
