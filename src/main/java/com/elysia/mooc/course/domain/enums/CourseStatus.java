package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程状态。 */
@Getter
@RequiredArgsConstructor
public enum CourseStatus implements BaseEnum<String> {

    /** 草稿。 */
    DRAFT("DRAFT", "草稿"),

    /** 审核中。 */
    PENDING("PENDING", "审核中"),

    /** 已发布。 */
    PUBLISHED("PUBLISHED", "已发布"),

    /** 已驳回。 */
    REJECTED("REJECTED", "已驳回"),

    /** 已下架。 */
    OFFLINE("OFFLINE", "已下架");

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
    public static CourseStatus of(Object value) {
        return BaseEnum.parse(CourseStatus.class, value);
    }
}
