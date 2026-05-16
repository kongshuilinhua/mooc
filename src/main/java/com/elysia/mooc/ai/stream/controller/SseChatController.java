package com.elysia.mooc.ai.stream.controller;

import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.stream.service.StreamingChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** AI SSE 流式响应控制器。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class SseChatController {

    private final StreamingChatService streamingChatService;

    /**
     * 普通聊天流式接口。
     *
     * @param request 普通聊天请求
     * @return SSE 连接
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody ChatRequest request) {
        return streamingChatService.streamChat(request);
    }

    /**
     * RAG 问答流式接口。
     *
     * @param request RAG 问答请求，message 为主字段，question 兼容旧字段
     * @return SSE 连接
     */
    @PostMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRag(@Valid @RequestBody RagChatRequest request) {
        return streamingChatService.streamRag(request);
    }
}
