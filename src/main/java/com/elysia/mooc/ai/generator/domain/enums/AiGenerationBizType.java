package com.elysia.mooc.ai.generator.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 生成业务类型。 */
@Getter
@RequiredArgsConstructor
public enum AiGenerationBizType implements BaseEnum<String> {

    /** 章节总结。 */
    CHAPTER_SUMMARY("CHAPTER_SUMMARY", "章节总结"),

    /** 练习题草稿。 */
    QUESTION_DRAFT("QUESTION_DRAFT", "练习题草稿"),

    /** 学习路径。 */
    LEARNING_PATH("LEARNING_PATH", "学习路径");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AiGenerationBizType of(Object value) {
        return BaseEnum.parse(AiGenerationBizType.class, value);
    }
}
