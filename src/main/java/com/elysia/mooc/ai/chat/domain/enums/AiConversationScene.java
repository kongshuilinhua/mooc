package com.elysia.mooc.ai.chat.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** AI 会话场景。 */
@Getter
@RequiredArgsConstructor
public enum AiConversationScene implements BaseEnum<String> {

    /** 普通聊天。 */
    CHAT("CHAT", "普通聊天"),

    /** RAG 问答，day16 后续复用。 */
    RAG("RAG", "知识库问答"),

    /** 课程助手，后续课程上下文复用。 */
    COURSE_ASSISTANT("COURSE_ASSISTANT", "课程助手");

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
    public static AiConversationScene of(Object value) {
        return BaseEnum.parse(AiConversationScene.class, value);
    }
}
