package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 消息类型。 */
@Getter
@RequiredArgsConstructor
public enum MessageType implements BaseEnum<String> {

    /** 系统消息。 */
    SYSTEM("SYSTEM", "系统消息"),

    /** 课程消息。 */
    COURSE("COURSE", "课程消息"),

    /** 审核消息。 */
    AUDIT("AUDIT", "审核消息"),

    /** AI 消息。 */
    AI("AI", "AI消息");

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
    public static MessageType of(Object value) {
        return BaseEnum.parse(MessageType.class, value);
    }
}
