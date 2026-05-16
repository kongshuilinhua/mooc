package com.elysia.mooc.ai.tool.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Tool 调用状态。 */
@Getter
@RequiredArgsConstructor
public enum ToolCallStatus implements BaseEnum<String> {

    /** 执行成功。 */
    SUCCESS("SUCCESS", "成功"),

    /** 执行失败。 */
    FAILED("FAILED", "失败");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ToolCallStatus of(Object value) {
        return BaseEnum.parse(ToolCallStatus.class, value);
    }
}
