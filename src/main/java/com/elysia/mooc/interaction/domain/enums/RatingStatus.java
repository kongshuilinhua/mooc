package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程评价状态。 */
@Getter
@RequiredArgsConstructor
public enum RatingStatus implements BaseEnum<String> {

    /** 正常展示。 */
    NORMAL("NORMAL", "正常"),

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
    public static RatingStatus of(Object value) {
        return BaseEnum.parse(RatingStatus.class, value);
    }
}
