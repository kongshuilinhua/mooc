package com.elysia.mooc.ai.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationDetailVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationVO;
import com.elysia.mooc.ai.chat.domain.vo.MessageVO;
import com.elysia.mooc.ai.chat.service.AiChatService;
import com.elysia.mooc.ai.chat.service.AiConversationService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** day15 普通聊天控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class AiChatControllerWebTest {

    @Mock
    private AiChatService aiChatService;

    @Mock
    private AiConversationService aiConversationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new AiChatController(aiChatService, aiConversationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chatShouldReturnDay15Contract() throws Exception {
        ChatResultVO result = new ChatResultVO();
        result.setConversationId(15001L);
        result.setMessageId(15102L);
        result.setContent("JWT 是一种无状态登录令牌。");
        result.setSources(Collections.emptyList());
        result.setToolCalls(Collections.emptyList());
        result.setStatus(AiMessageStatus.SUCCESS);
        result.setModelName("qwen-plus");
        result.setPromptTokens(20);
        result.setCompletionTokens(10);
        result.setTotalTokens(30);
        result.setFinishReason("stop");
        when(aiChatService.chat(any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"请解释 JWT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(15001))
                .andExpect(jsonPath("$.data.messageId").value(15102))
                .andExpect(jsonPath("$.data.content").value("JWT 是一种无状态登录令牌。"))
                .andExpect(jsonPath("$.data.sources").isArray())
                .andExpect(jsonPath("$.data.toolCalls").isArray())
                .andExpect(jsonPath("$.data.modelName").value("qwen-plus"));

        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(aiChatService).chat(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getMessage()).isEqualTo("请解释 JWT");
    }

    @Test
    void chatShouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("聊天内容不能为空"));
    }

    @Test
    void listConversationsShouldReturnPageResult() throws Exception {
        ConversationVO conversation = conversation();
        when(aiConversationService.listConversations(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(conversation)));

        mockMvc.perform(get("/api/ai/conversations?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(15001))
                .andExpect(jsonPath("$.data.list[0].scene").value("CHAT"));
    }

    @Test
    void getConversationShouldReturnMessages() throws Exception {
        ConversationDetailVO detail = new ConversationDetailVO();
        detail.setId(15001L);
        detail.setTitle("JWT 登录机制咨询");
        detail.setScene(AiConversationScene.CHAT);
        MessageVO message = new MessageVO();
        message.setId(15101L);
        message.setConversationId(15001L);
        message.setRole(AiMessageRole.USER);
        message.setStatus(AiMessageStatus.SUCCESS);
        message.setContent("JWT 是什么");
        message.setSources(Collections.emptyList());
        message.setToolCalls(Collections.emptyList());
        detail.setMessages(List.of(message));
        when(aiConversationService.getConversation(15001L)).thenReturn(detail);

        mockMvc.perform(get("/api/ai/conversations/15001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(15001))
                .andExpect(jsonPath("$.data.messages[0].role").value("USER"))
                .andExpect(jsonPath("$.data.messages[0].sources").isArray());
    }

    @Test
    void getConversationShouldReturn403WhenServiceRejectsOwner() throws Exception {
        when(aiConversationService.getConversation(15001L))
                .thenThrow(new BizException(AiChatErrorCode.AI_CHAT_FORBIDDEN));

        mockMvc.perform(get("/api/ai/conversations/15001"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(AiChatErrorCode.AI_CHAT_FORBIDDEN.message()));
    }

    @Test
    void deleteConversationShouldReturnTrue() throws Exception {
        when(aiConversationService.deleteConversation(15001L)).thenReturn(true);

        mockMvc.perform(delete("/api/ai/conversations/15001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    private ConversationVO conversation() {
        ConversationVO vo = new ConversationVO();
        vo.setId(15001L);
        vo.setTitle("JWT 登录机制咨询");
        vo.setScene(AiConversationScene.CHAT);
        vo.setLastMessageTime(LocalDateTime.of(2026, 5, 16, 10, 0));
        vo.setUpdateTime(LocalDateTime.of(2026, 5, 16, 10, 0));
        return vo;
    }
}
