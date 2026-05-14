package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 问题状态。 */
@Getter
@RequiredArgsConstructor
public enum QuestionStatus implements BaseEnum<String> {

    /** 待解决。 */
    OPEN("OPEN", "待解决"),

    /** 已解决。 */
    RESOLVED("RESOLVED", "已解决"),

    /** 已隐藏。 */
    HIDDEN("HIDDEN", "已隐藏");

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
    public static QuestionStatus of(Object value) {
        return BaseEnum.parse(QuestionStatus.class, value);
    }
}
