package com.elysia.mooc.ai.chat.domain.dto;

import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建 AI 会话请求，当前主要为后续扩展预留。 */
@Data
public class CreateConversationRequest {

    /** 会话标题，不传时由首条用户消息生成。 */
    @Size(max = 128, message = "会话标题不能超过128个字符")
    private String title;

    /** 会话场景，默认普通聊天。 */
    private AiConversationScene scene = AiConversationScene.CHAT;

    /** 绑定课程 ID。 */
    private Long courseId;
}
