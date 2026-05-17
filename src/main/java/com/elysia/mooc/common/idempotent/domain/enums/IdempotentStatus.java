package com.elysia.mooc.common.idempotent.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 幂等处理状态。 */
@Getter
@RequiredArgsConstructor
public enum IdempotentStatus implements BaseEnum<Integer> {

    /** 处理中。 */
    PROCESSING(0, "处理中"),

    /** 处理成功。 */
    SUCCESS(1, "成功"),

    /** 处理失败。 */
    FAILED(2, "失败");

    /** 落库值。 */
    @EnumValue
    private final Integer value;

    /** 中文说明。 */
    private final String desc;

    @Override
    @JsonValue
    public Integer getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IdempotentStatus of(Object value) {
        return BaseEnum.parse(IdempotentStatus.class, value);
    }
}
