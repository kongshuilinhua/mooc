package com.elysia.mooc.common.api;

import com.elysia.mooc.common.trace.TraceContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口响应结构，所有 Controller 默认返回该对象。
 *
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    /** 业务状态码。 */
    private Integer code;

    /** 中文响应提示。 */
    private String message;

    /** 业务响应数据。 */
    private T data;

    /** 当前请求链路追踪 ID。 */
    private String traceId;

    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 统一成功响应
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, "操作成功", data, TraceContext.getTraceId());
    }

    /**
     * 构造异步任务已受理响应。
     *
     * @param data 任务受理结果
     * @param <T> 响应数据类型
     * @return 统一异步受理响应
     */
    public static <T> ApiResult<T> accepted(T data) {
        return new ApiResult<>(202, "请求已受理", data, TraceContext.getTraceId());
    }

    /**
     * 构造失败响应。
     *
     * @param code 错误码
     * @param message 中文错误提示
     * @param <T> 响应数据类型
     * @return 统一失败响应
     */
    public static <T> ApiResult<T> fail(Integer code, String message) {
        return new ApiResult<>(code, message, null, TraceContext.getTraceId());
    }

    /**
     * 构造带错误明细的失败响应。
     *
     * @param code 错误码
     * @param message 中文错误提示
     * @param data 错误明细
     * @param <T> 响应数据类型
     * @return 统一失败响应
     */
    public static <T> ApiResult<T> fail(Integer code, String message, T data) {
        return new ApiResult<>(code, message, data, TraceContext.getTraceId());
    }
}
