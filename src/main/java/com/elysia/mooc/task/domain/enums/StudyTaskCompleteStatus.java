package com.elysia.mooc.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习任务实例完成状态。 */
@Getter
@RequiredArgsConstructor
public enum StudyTaskCompleteStatus implements BaseEnum<String> {

    /** 待完成。 */
    TODO("TODO", "待完成"),

    /** 进行中。 */
    DOING("DOING", "进行中"),

    /** 已完成。 */
    DONE("DONE", "已完成");

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
    public static StudyTaskCompleteStatus of(Object value) {
        return BaseEnum.parse(StudyTaskCompleteStatus.class, value);
    }
}
