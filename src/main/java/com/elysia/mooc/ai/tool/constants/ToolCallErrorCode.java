package com.elysia.mooc.ai.tool.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;

/** Tool Calling 业务错误码。 */
@RequiredArgsConstructor
public enum ToolCallErrorCode implements ErrorCode {

    /** 工具参数错误。 */
    TOOL_PARAM_INVALID(180400, "工具参数不正确", 400),

    /** 工具不存在。 */
    TOOL_NOT_FOUND(180404, "工具不存在或未启用", 400),

    /** 工具访问越权。 */
    TOOL_FORBIDDEN(180403, "没有权限调用该工具", 403),

    /** 工具执行失败。 */
    TOOL_EXECUTE_FAILED(180500, "工具执行失败，请稍后重试", 400);

    private final int code;
    private final String message;
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
