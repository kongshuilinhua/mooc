package com.elysia.mooc.studyarchive.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 错题掌握程度。 */
@Getter
@RequiredArgsConstructor
public enum WrongBookMasteryLevel implements BaseEnum<String> {

    /** 掌握较弱，需要优先复习。 */
    LOW("LOW", "薄弱"),

    /** 基本掌握，仍需巩固。 */
    MEDIUM("MEDIUM", "一般"),

    /** 掌握较好。 */
    HIGH("HIGH", "较好");

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
    public static WrongBookMasteryLevel of(Object value) {
        return BaseEnum.parse(WrongBookMasteryLevel.class, value);
    }
}
