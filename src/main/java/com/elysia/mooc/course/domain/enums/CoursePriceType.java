package com.elysia.mooc.course.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 课程价格类型。 */
@Getter
@RequiredArgsConstructor
public enum CoursePriceType implements BaseEnum<String> {

    /** 免费课程。 */
    FREE("FREE", "免费"),

    /** 付费课程。 */
    PAID("PAID", "付费");

    /** 接口输出值。 */
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
    public static CoursePriceType of(Object value) {
        return BaseEnum.parse(CoursePriceType.class, value);
    }
}
