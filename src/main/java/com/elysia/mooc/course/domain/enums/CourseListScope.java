package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程列表查询范围。 */
@Getter
@RequiredArgsConstructor
public enum CourseListScope implements BaseEnum<String> {

    /** 公开课程。 */
    PUBLIC("PUBLIC", "公开课程"),

    /** 当前教师自己的课程。 */
    MINE("MINE", "我的课程"),

    /** 管理端全部课程。 */
    ALL("ALL", "全部课程");

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
    public static CourseListScope of(Object value) {
        return BaseEnum.parse(CourseListScope.class, value);
    }
}
