package com.elysia.mooc.exam.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 题目难度。 */
@Getter
@RequiredArgsConstructor
public enum ExamDifficulty implements BaseEnum<String> {

    /** 入门。 */
    BEGINNER("BEGINNER", "入门"),

    /** 简单。 */
    EASY("EASY", "简单"),

    /** 中等。 */
    MEDIUM("MEDIUM", "中等"),

    /** 困难。 */
    HARD("HARD", "困难");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ExamDifficulty of(Object value) {
        return BaseEnum.parse(ExamDifficulty.class, normalizeLegacy(value));
    }

    private static Object normalizeLegacy(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if ("INTERMEDIATE".equalsIgnoreCase(text)) {
            return MEDIUM.value;
        }
        if ("ADVANCED".equalsIgnoreCase(text)) {
            return HARD.value;
        }
        return value;
    }
}
