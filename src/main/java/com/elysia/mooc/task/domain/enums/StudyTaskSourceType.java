package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务来源类型。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskSourceType implements BaseEnum<String> {

    /** 课程学习任务。 */
    COURSE("COURSE", "课程学习"),

    /** 作业任务。 */
    HOMEWORK("HOMEWORK", "作业任务"),

    /** 考试任务。 */
    EXAM("EXAM", "考试任务"),

    /** 手动添加任务。 */
    MANUAL("MANUAL", "手动任务");

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
    public static StudyTaskSourceType of(Object value) {
        return BaseEnum.parse(StudyTaskSourceType.class, value);
    }
}
