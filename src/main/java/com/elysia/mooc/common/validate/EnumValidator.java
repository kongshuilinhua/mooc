package com.elysia.mooc.common.validate;

import com.elysia.mooc.common.enums.BaseEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** 业务枚举校验器。 */
public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

    private Class<? extends Enum<?>> enumClass;
    private boolean allowNull;

    @Override
    public void initialize(EnumValid annotation) {
        this.enumClass = annotation.enumClass();
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return allowNull;
        }
        return BaseEnum.isValid(enumClass, value);
    }
}
