package com.elysia.mooc.ai.chat.service;

import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;

/** AI 普通聊天服务。 */
public interface AiChatService {

    /**
     * 发送普通聊天消息。
     *
     * @param request 聊天请求
     * @return AI 回复结果
     */
    ChatResultVO chat(ChatRequest request);
}
