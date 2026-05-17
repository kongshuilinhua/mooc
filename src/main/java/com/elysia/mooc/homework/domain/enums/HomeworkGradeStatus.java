package com.elysia.mooc.homework.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 作业批改状态。 */
@Getter
@RequiredArgsConstructor
public enum HomeworkGradeStatus implements BaseEnum<String> {

    /** 待批改。 */
    PENDING("PENDING", "待批改"),

    /** 已批改。 */
    GRADED("GRADED", "已批改");

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
    public static HomeworkGradeStatus of(Object value) {
        return BaseEnum.parse(HomeworkGradeStatus.class, value);
    }
}
