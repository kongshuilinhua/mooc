package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 运营配置启停状态。 */
@Getter
@RequiredArgsConstructor
public enum OpsConfigStatus implements BaseEnum<String> {

    /** 启用。 */
    ENABLED("ENABLED", "启用"),

    /** 禁用。 */
    DISABLED("DISABLED", "禁用");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsConfigStatus of(Object value) {
        return BaseEnum.parse(OpsConfigStatus.class, value);
    }
}
