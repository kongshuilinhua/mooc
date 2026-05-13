package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程排序方式。 */
@Getter
@RequiredArgsConstructor
public enum CourseSort implements BaseEnum<String> {

    /** 最新更新。 */
    NEWEST("NEWEST", "最新"),

    /** 热门课程。 */
    HOT("HOT", "热门"),

    /** 评分优先。 */
    RATING("RATING", "评分");

    /** 接口输入输出值。 */
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
    public static CourseSort of(Object value) {
        return BaseEnum.parse(CourseSort.class, value);
    }
}
