package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务优先级。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskPriorityLevel implements BaseEnum<String> {

    /** 低优先级。 */
    LOW("LOW", "低"),

    /** 普通优先级。 */
    NORMAL("NORMAL", "普通"),

    /** 高优先级。 */
    HIGH("HIGH", "高");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StudyTaskPriorityLevel of(Object value) {
        return BaseEnum.parse(StudyTaskPriorityLevel.class, value);
    }
}
