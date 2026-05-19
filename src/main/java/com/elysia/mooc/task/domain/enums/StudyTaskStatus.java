package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务计划状态。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskStatus implements BaseEnum<String> {

    /** 待开始。 */
    PENDING("PENDING", "待开始"),

    /** 进行中。 */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /** 已完成。 */
    FINISHED("FINISHED", "已完成");

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
    public static StudyTaskStatus of(Object value) {
        return BaseEnum.parse(StudyTaskStatus.class, value);
    }
}
