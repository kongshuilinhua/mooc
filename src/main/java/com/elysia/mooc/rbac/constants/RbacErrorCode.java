package com.elysia.mooc.rbac.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RBAC 模块业务错误码。
 * 错误码区间 3000-3999，每个枚举值包含数字编码和中文提示。
 */
@Getter
@RequiredArgsConstructor
public enum RbacErrorCode implements ErrorCode {

    /** 角色不存在 */
    ROLE_NOT_FOUND(3001, "角色不存在"),

    /** 角色编码已被占用 */
    ROLE_CODE_EXISTS(3002, "角色编码已存在"),

    /** 用户不存在 */
    USER_NOT_FOUND(3003, "用户不存在"),

    /** 权限不足 */
    PERMISSION_DENIED(3004, "没有权限访问该资源"),

    /** 角色ID无效 */
    ROLE_INVALID(3005, "无效的角色"),
    ;

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
