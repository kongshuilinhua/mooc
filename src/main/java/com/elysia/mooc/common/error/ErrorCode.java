package com.elysia.mooc.common.error;

/**
 * 统一错误码抽象，业务异常和全局异常处理统一依赖该接口。
 */
public interface ErrorCode {

    /**
     * 获取业务错误码。
     *
     * @return 错误码
     */
    int code();

    /**
     * 获取中文错误提示。
     *
     * @return 错误提示
     */
    String message();
}
