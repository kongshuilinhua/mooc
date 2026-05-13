package com.elysia.mooc.common.error;

/**
 * 通用错误码，覆盖基础异常场景。
 */
public enum CommonErrorCode implements ErrorCode {

    /** 参数错误。 */
    PARAM_INVALID(400, "参数错误"),

    /** 未登录或登录状态失效。 */
    UNAUTHORIZED(401, "请先登录"),

    /** 当前用户无访问权限。 */
    FORBIDDEN(403, "没有权限访问该资源"),

    /** 资源不存在。 */
    NOT_FOUND(404, "资源不存在"),

    /** 数据状态不允许当前操作。 */
    CONFLICT(409, "业务状态冲突"),

    /** 请求频率超过限制。 */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    /** 系统内部异常。 */
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    /** 外部服务调用异常。 */
    EXTERNAL_SERVICE_ERROR(502, "外部服务暂不可用");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务错误码。
     *
     * @return 错误码
     */
    @Override
    public int code() {
        return code;
    }

    /**
     * 获取中文错误提示。
     *
     * @return 错误提示
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * 通用错误码本身使用标准 HTTP 状态码，直接作为响应状态。
     *
     * @return HTTP 状态码
     */
    @Override
    public int httpStatus() {
        return code;
    }
}
