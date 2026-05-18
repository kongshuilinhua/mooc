package com.elysia.mooc.studyarchive.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 错题来源类型。 */
@Getter
@RequiredArgsConstructor
public enum WrongBookSourceType implements BaseEnum<String> {

    /** 考试错题。 */
    EXAM("EXAM", "考试"),

    /** 练习错题。 */
    PRACTICE("PRACTICE", "练习"),

    /** 作业错题。 */
    HOMEWORK("HOMEWORK", "作业");

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
    public static WrongBookSourceType of(Object value) {
        return BaseEnum.parse(WrongBookSourceType.class, value);
    }
}
