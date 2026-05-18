package com.elysia.mooc.studyarchive.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 学习笔记状态。 */
@Getter
@RequiredArgsConstructor
public enum LearningNoteStatus implements BaseEnum<String> {

    /** 正常可见。 */
    NORMAL("NORMAL", "正常"),

    /** 已归档，暂不在默认列表展示。 */
    ARCHIVED("ARCHIVED", "已归档");

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
    public static LearningNoteStatus of(Object value) {
        return BaseEnum.parse(LearningNoteStatus.class, value);
    }
}
