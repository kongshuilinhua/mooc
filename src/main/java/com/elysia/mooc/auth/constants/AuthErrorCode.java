package com.elysia.mooc.auth.constants;

import com.elysia.mooc.common.error.ErrorCode;

/**
 * 认证模块错误码，所有提示面向用户返回中文。
 */
public enum AuthErrorCode implements ErrorCode {

    /** 用户名已存在。 */
    AUTH_USERNAME_EXISTS(10001, "用户名已存在"),

    /** 邮箱已存在。 */
    AUTH_EMAIL_EXISTS(10002, "邮箱已存在"),

    /** 手机号已存在。 */
    AUTH_PHONE_EXISTS(10003, "手机号已存在"),

    /** 用户名或密码错误。 */
    AUTH_BAD_CREDENTIALS(10004, "用户名或密码错误"),

    /** 账号已被禁用。 */
    AUTH_USER_DISABLED(10005, "账号已被禁用"),

    /** 登录状态已过期。 */
    AUTH_TOKEN_EXPIRED(10006, "登录状态已过期"),

    /** 刷新令牌无效。 */
    AUTH_REFRESH_TOKEN_INVALID(10007, "刷新令牌无效"),

    /** 用户未登录。 */
    AUTH_LOGIN_REQUIRED(10008, "请先登录"),

    /** 当前账号不可用。 */
    AUTH_USER_UNAVAILABLE(10009, "账号不可用，请重新登录");

    private final int code;
    private final String message;

    AuthErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    @Override
    public int code() {
        return code;
    }

    /**
     * 获取中文错误提示。
     *
     * @return 中文错误提示
     */
    @Override
    public String message() {
        return message;
    }
}
