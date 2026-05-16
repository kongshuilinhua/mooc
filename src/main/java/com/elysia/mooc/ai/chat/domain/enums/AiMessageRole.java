package com.elysia.mooc.ai.chat.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 消息角色。 */
@Getter
@RequiredArgsConstructor
public enum AiMessageRole implements BaseEnum<String> {

    /** 用户消息。 */
    USER("USER", "用户"),

    /** 助手消息。 */
    ASSISTANT("ASSISTANT", "助手"),

    /** 系统提示词。 */
    SYSTEM("SYSTEM", "系统"),

    /** 工具调用结果。 */
    TOOL("TOOL", "工具");

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
    public static AiMessageRole of(Object value) {
        return BaseEnum.parse(AiMessageRole.class, value);
    }
}
