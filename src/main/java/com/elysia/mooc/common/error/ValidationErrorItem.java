package com.elysia.mooc.common.error;

/**
 * 字段校验错误明细，全局异常处理器用于返回参数校验失败的具体位置。
 */

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationErrorItem {

    /** 错误字段名，非字段级错误时使用“请求”。 */
    private String field;

    /** 中文字段校验错误信息。 */
    private String message;
}
