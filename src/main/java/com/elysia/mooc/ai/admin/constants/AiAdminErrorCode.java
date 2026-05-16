package com.elysia.mooc.ai.admin.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 管理后台错误码。 */
@Getter
@RequiredArgsConstructor
public enum AiAdminErrorCode implements ErrorCode {

    /** 请求参数不合法。 */
    AI_ADMIN_PARAM_INVALID(19001, "AI 管理参数不正确", 400),

    /** 模型配置不存在。 */
    AI_MODEL_CONFIG_NOT_FOUND(19002, "模型配置不存在", 404),

    /** 无权访问 AI 管理后台。 */
    AI_ADMIN_FORBIDDEN(19003, "没有权限访问 AI 管理后台", 403),

    /** 模型配置状态不允许当前操作。 */
    AI_MODEL_CONFIG_STATUS_INVALID(19004, "模型配置状态不允许当前操作", 409);

    /** 业务错误码。 */
    private final int code;

    /** 中文错误提示。 */
    private final String message;

    /** HTTP 状态码。 */
    private final int httpStatus;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
