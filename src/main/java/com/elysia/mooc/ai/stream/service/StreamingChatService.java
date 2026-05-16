package com.elysia.mooc.ai.stream.service;

import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** AI 流式响应服务。 */
public interface StreamingChatService {

    /**
     * 普通聊天流式生成。
     *
     * @param request 聊天请求
     * @return SSE 连接
     */
    SseEmitter streamChat(ChatRequest request);

    /**
     * RAG 问答流式生成。
     *
     * @param request RAG 请求
     * @return SSE 连接
     */
    SseEmitter streamRag(RagChatRequest request);
}
