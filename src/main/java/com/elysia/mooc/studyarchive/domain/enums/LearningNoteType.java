package com.elysia.mooc.studyarchive.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习笔记类型。 */
@Getter
@RequiredArgsConstructor
public enum LearningNoteType implements BaseEnum<String> {

    /** 普通文字笔记。 */
    TEXT("TEXT", "文字笔记"),

    /** 重点标记。 */
    HIGHLIGHT("HIGHLIGHT", "重点标记"),

    /** 疑问记录。 */
    QUESTION("QUESTION", "疑问记录");

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
    public static LearningNoteType of(Object value) {
        return BaseEnum.parse(LearningNoteType.class, value);
    }
}
