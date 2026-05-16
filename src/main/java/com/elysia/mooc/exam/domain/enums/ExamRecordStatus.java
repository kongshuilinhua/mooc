package com.elysia.mooc.exam.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 作答记录状态。 */
@Getter
@RequiredArgsConstructor
public enum ExamRecordStatus implements BaseEnum<String> {

    /** 作答中。 */
    DOING("DOING", "作答中"),

    /** 已提交，包含待人工批改内容。 */
    SUBMITTED("SUBMITTED", "已提交"),

    /** 已完成判分。 */
    GRADED("GRADED", "已判分");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamRecordStatus of(Object value) {
        return BaseEnum.parse(ExamRecordStatus.class, value);
    }
}
