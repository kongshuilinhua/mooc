package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 运营配置值类型。 */
@Getter
@RequiredArgsConstructor
public enum OpsConfigValueType implements BaseEnum<String> {

    /** 字符串。 */
    STRING("STRING", "字符串"),

    /** 数字。 */
    NUMBER("NUMBER", "数字"),

    /** 布尔值。 */
    BOOLEAN("BOOLEAN", "布尔值"),

    /** JSON 文本。 */
    JSON("JSON", "JSON");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsConfigValueType of(Object value) {
        return BaseEnum.parse(OpsConfigValueType.class, value);
    }
}
