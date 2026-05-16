package com.elysia.mooc.ai.chat.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 普通聊天请求。 */
@Data
public class ChatRequest {

    /** 会话 ID，不传则自动创建普通聊天会话。 */
    private Long conversationId;

    /** 用户输入的问题。 */
    @NotBlank(message = "聊天内容不能为空")
    @Size(max = 4000, message = "聊天内容不能超过4000个字符")
    private String message;

    /** 前端上下文类型，day15 仅透传课程上下文，不做 RAG。 */
    private String contextType;

    /** 前端上下文 ID。 */
    private Long contextId;

    /** 课程 ID，用于给会话打业务上下文。 */
    private Long courseId;

    /** 是否流式，day15 普通聊天忽略该字段。 */
    private Boolean stream;
}
