package com.elysia.mooc.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关键写操作审计注解。
 * 用于把高风险操作的操作者、目标对象、请求路径、执行结果和耗时统一落库。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /** 操作动作编码，例如 USER_DISABLE、COURSE_APPROVE。 */
    String action();

    /** 操作目标类型，例如 USER、COURSE、ORDER。 */
    String targetType();

    /** 目标 ID 的 SpEL 表达式，例如 #userId、#courseId。 */
    String targetId() default "";
}
