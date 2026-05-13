package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程难度。 */
@Getter
@RequiredArgsConstructor
public enum CourseDifficulty implements BaseEnum<String> {

    /** 入门。 */
    BEGINNER("BEGINNER", "入门"),

    /** 进阶。 */
    INTERMEDIATE("INTERMEDIATE", "进阶"),

    /** 高级。 */
    ADVANCED("ADVANCED", "高级");

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
    public static CourseDifficulty of(Object value) {
        return BaseEnum.parse(CourseDifficulty.class, value);
    }
}
