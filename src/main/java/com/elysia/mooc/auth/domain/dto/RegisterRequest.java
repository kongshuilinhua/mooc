package com.elysia.mooc.auth.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求参数。
 */
@Data
public class RegisterRequest {

    /** 登录用户名。 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度必须在4到32位之间")
    private String username;

    /** 登录密码。 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度必须在8到64位之间")
    private String password;

    /** 用户昵称。 */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称长度不能超过64位")
    private String nickname;

    /** 邮箱。 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128位")
    private String email;

    /** 手机号。 */
    @Size(max = 32, message = "手机号长度不能超过32位")
    private String phone;
}
