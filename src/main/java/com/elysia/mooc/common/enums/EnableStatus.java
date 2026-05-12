package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 通用启停状态，数据库值：0 禁用，1 启用。 */
@Getter
@RequiredArgsConstructor
public enum EnableStatus implements BaseEnum<Integer> {

    /** 禁用。 */
    DISABLED(0, "禁用"),

    /** 启用。 */
    ENABLED(1, "启用");

    /** 落库和接口输出值。 */
    @EnumValue
    private final Integer value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public Integer getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EnableStatus of(Object value) {
        return BaseEnum.parse(EnableStatus.class, value);
    }
}
