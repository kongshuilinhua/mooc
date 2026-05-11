package com.elysia.mooc.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退出登录请求参数。
 */
@Data
public class LogoutRequest {

    /** 需要撤销的刷新令牌明文。 */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
