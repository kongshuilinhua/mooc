package com.elysia.mooc.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录请求参数。
 */
@Data
public class LoginRequest {

    /** 用户名、邮箱或手机号。 */
    @NotBlank(message = "账号不能为空")
    private String username;

    /** 登录密码。 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 设备标识。 */
    @Size(max = 128, message = "设备标识长度不能超过128位")
    private String deviceId;

    /** 客户端类型。 */
    @Size(max = 32, message = "客户端类型长度不能超过32位")
    private String clientType;
}
