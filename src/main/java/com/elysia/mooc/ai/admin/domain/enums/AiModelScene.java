package com.elysia.mooc.ai.admin.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 模型使用场景。 */
@Getter
@RequiredArgsConstructor
public enum AiModelScene implements BaseEnum<String> {

    /** 普通聊天场景。 */
    CHAT("CHAT", "普通聊天"),

    /** 向量化场景。 */
    EMBEDDING("EMBEDDING", "向量化"),

    /** RAG 问答场景。 */
    RAG("RAG", "RAG 问答");

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
    public static AiModelScene of(Object value) {
        return BaseEnum.parse(AiModelScene.class, value);
    }
}
