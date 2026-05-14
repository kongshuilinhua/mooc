package com.elysia.mooc.learning.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 加入课程来源。 */
@Getter
@RequiredArgsConstructor
public enum LearningSource implements BaseEnum<String> {

    /** 免费课程加入。 */
    FREE("FREE", "免费加入"),

    /** 购买后加入。 */
    PURCHASE("PURCHASE", "购买加入"),

    /** 教师预览加入。 */
    TEACHER_PREVIEW("TEACHER_PREVIEW", "教师预览");

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
    public static LearningSource of(Object value) {
        return BaseEnum.parse(LearningSource.class, value);
    }
}
