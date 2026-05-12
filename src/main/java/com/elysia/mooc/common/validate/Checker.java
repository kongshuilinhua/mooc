package com.elysia.mooc.common.validate;

/** 请求参数自校验契约，用于表达跨字段业务规则。 */
public interface Checker {

    /** 执行自校验，校验失败时抛出业务异常。 */
    void check();
}
