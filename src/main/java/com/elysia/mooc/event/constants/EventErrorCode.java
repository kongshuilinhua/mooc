package com.elysia.mooc.event.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 事件基础设施错误码。 */
@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {

    /** 事件参数不合法。 */
    EVENT_PARAM_INVALID(11100, "事件参数不合法", 400),

    /** 事件不存在。 */
    EVENT_NOT_FOUND(11101, "事件不存在", 404),

    /** 事件状态不允许当前操作。 */
    EVENT_STATUS_INVALID(11102, "事件状态不允许当前操作", 409),

    /** 事件ID已存在。 */
    EVENT_DUPLICATED(11103, "事件ID已存在", 409),

    /** Kafka 发送失败。 */
    EVENT_SEND_FAILED(11104, "事件发送失败，请稍后重试", 502);

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
