package com.elysia.mooc.ai.stream.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.stream.service.StreamingChatService;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** day17 SSE 控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class SseChatControllerWebTest {

    @Mock
    private StreamingChatService streamingChatService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SseChatController(streamingChatService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void streamChatShouldReturnSseContentType() throws Exception {
        when(streamingChatService.streamChat(any())).thenReturn(completedEmitter());

        MvcResult result = mockMvc.perform(post("/api/ai/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"解释 Java 接口\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    void streamRagShouldReturnSseContentType() throws Exception {
        when(streamingChatService.streamRag(any())).thenReturn(completedEmitter());

        MvcResult result = mockMvc.perform(post("/api/ai/rag/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"RAG 为什么切片？\",\"knowledgeBaseId\":12002,\"topK\":5}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    private SseEmitter completedEmitter() {
        SseEmitter emitter = new SseEmitter(1000L);
        emitter.complete();
        return emitter;
    }
}
