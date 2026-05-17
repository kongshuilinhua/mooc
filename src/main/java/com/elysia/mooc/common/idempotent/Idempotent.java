package com.elysia.mooc.common.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 写接口幂等注解。
 * 通过业务类型、当前用户和 X-Idempotency-Key 隔离请求，避免不同用户共用同一个 key。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /** 业务类型，例如 ORDER_CREATE、MOCK_PAY。 */
    String bizType();

    /** 业务 ID 的 SpEL 表达式，例如 #orderId、#request.courseId。 */
    String bizId() default "";

    /** 幂等记录有效期，单位秒。 */
    long expireSeconds() default 86_400;
}
