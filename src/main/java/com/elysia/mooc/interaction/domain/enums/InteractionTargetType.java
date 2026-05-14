package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 互动目标类型。 */
@Getter
@RequiredArgsConstructor
public enum InteractionTargetType implements BaseEnum<String> {

    /** 问题。 */
    QUESTION("QUESTION", "问题"),

    /** 回答。 */
    ANSWER("ANSWER", "回答"),

    /** 课程。 */
    COURSE("COURSE", "课程"),

    /** 评论，预留给后续楼中楼评论。 */
    COMMENT("COMMENT", "评论");

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
    public static InteractionTargetType of(Object value) {
        return BaseEnum.parse(InteractionTargetType.class, value);
    }
}
