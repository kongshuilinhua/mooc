package com.elysia.mooc.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 刷新 Token 请求参数。
 */
@Data
public class RefreshTokenRequest {

    /** 刷新令牌明文。 */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    /** 设备标识。 */
    @Size(max = 128, message = "设备标识长度不能超过128位")
    private String deviceId;
}
