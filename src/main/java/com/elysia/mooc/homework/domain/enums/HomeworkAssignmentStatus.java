package com.elysia.mooc.homework.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 作业发布状态。 */
@Getter
@RequiredArgsConstructor
public enum HomeworkAssignmentStatus implements BaseEnum<String> {

    /** 草稿，学生不可提交。 */
    DRAFT("DRAFT", "草稿"),

    /** 已发布，学生可在截止前提交。 */
    PUBLISHED("PUBLISHED", "已发布");

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
    public static HomeworkAssignmentStatus of(Object value) {
        return BaseEnum.parse(HomeworkAssignmentStatus.class, value);
    }
}
