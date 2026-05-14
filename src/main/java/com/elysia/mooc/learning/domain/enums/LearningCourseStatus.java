package com.elysia.mooc.learning.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 我的课程学习状态。 */
@Getter
@RequiredArgsConstructor
public enum LearningCourseStatus implements BaseEnum<String> {

    /** 学习中。 */
    LEARNING("LEARNING", "学习中"),

    /** 已完成。 */
    COMPLETED("COMPLETED", "已完成");

    /** 接口筛选和输出值。 */
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
    public static LearningCourseStatus of(Object value) {
        return BaseEnum.parse(LearningCourseStatus.class, value);
    }
}
