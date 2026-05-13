package com.elysia.mooc.common.exception;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;

/**
 * 业务异常，携带统一错误码和中文提示，用于主动中断不满足业务规则的流程。
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务错误码。 */
    private final int code;

    /** HTTP 状态码。 */
    private final int httpStatus;

    /**
     * 根据错误码创建业务异常。
     *
     * @param errorCode 统一错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.message());
        this.code = errorCode.code();
        this.httpStatus = errorCode.httpStatus();
    }

    /**
     * 根据错误码和自定义中文提示创建业务异常。
     *
     * @param errorCode 统一错误码
     * @param message 中文错误提示
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.code();
        this.httpStatus = errorCode.httpStatus();
    }
}
