package com.elysia.mooc.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 业务枚举参数校验注解，支持枚举对象、数字值和字符串编码。 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValidator.class)
public @interface EnumValid {

    /** 中文错误提示。 */
    String message() default "枚举值不合法";

    /** 需要校验的业务枚举类型。 */
    Class<? extends Enum<?>> enumClass();

    /** 是否允许空值。 */
    boolean allowNull() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
