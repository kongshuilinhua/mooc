package com.elysia.mooc.exam.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 题目类型。 */
@Getter
@RequiredArgsConstructor
public enum ExamQuestionType implements BaseEnum<String> {

    /** 单选题。 */
    SINGLE("SINGLE", "单选题"),

    /** 多选题。 */
    MULTI("MULTI", "多选题"),

    /** 判断题。 */
    JUDGE("JUDGE", "判断题"),

    /** 简答题。 */
    SHORT("SHORT", "简答题");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamQuestionType of(Object value) {
        return BaseEnum.parse(ExamQuestionType.class, value);
    }
}
