package com.elysia.mooc.ai.chat.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionRequest;
import com.elysia.mooc.ai.model.ChatCompletionResult;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** day15 普通聊天服务业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private AiConversationMapper conversationMapper;

    @Mock
    private AiMessageMapper messageMapper;

    @Mock
    private AiChatClient aiChatClient;

    private AiChatServiceImpl aiChatService;
    private AiChatProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AiChatProperties();
        properties.setModel("qwen-plus");
        properties.setHistoryLimit(10);
        aiChatService = new AiChatServiceImpl(
                userContextService,
                conversationMapper,
                messageMapper,
                properties,
                aiChatClient);
    }

    @Test
    void chatShouldCreateConversationAndSaveUserAndAssistantMessages() {
        when(userContextService.currentUserId()).thenReturn(4L);
        mockConversationInsert(15001L);
        mockMessageInsert();
        when(messageMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AiMessagePO> page = invocation.getArgument(0);
            page.setRecords(List.of(userMessage(15101L, 15001L, 4L, "请解释 JWT")));
            return page;
        });
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenReturn(new ChatCompletionResult("JWT 是一种无状态登录令牌。", "qwen-plus", 20, 10, 30, "stop"));

        ChatRequest request = new ChatRequest();
        request.setMessage("请解释 JWT");
        ChatResultVO result = aiChatService.chat(request);

        assertThat(result.getConversationId()).isEqualTo(15001L);
        assertThat(result.getMessageId()).isEqualTo(15102L);
        assertThat(result.getContent()).isEqualTo("JWT 是一种无状态登录令牌。");
        assertThat(result.getSources()).isEmpty();
        assertThat(result.getToolCalls()).isEmpty();
        ArgumentCaptor<AiConversationPO> conversationCaptor = ArgumentCaptor.forClass(AiConversationPO.class);
        verify(conversationMapper).insert(conversationCaptor.capture());
        assertThat(conversationCaptor.getValue().getScene()).isEqualTo(AiConversationScene.CHAT);
        assertThat(conversationCaptor.getValue().getMemoryStrategy()).isEqualTo("RECENT_N");

        ArgumentCaptor<AiMessagePO> messageCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues().get(0).getRole()).isEqualTo(AiMessageRole.USER);
        assertThat(messageCaptor.getAllValues().get(1).getRole()).isEqualTo(AiMessageRole.ASSISTANT);
        assertThat(messageCaptor.getAllValues().get(1).getStatus()).isEqualTo(AiMessageStatus.SUCCESS);
    }

    @Test
    void chatShouldRejectConversationOfOtherUser() {
        when(userContextService.currentUserId()).thenReturn(4L);
        AiConversationPO other = conversation(15001L, 5L);
        when(conversationMapper.selectById(15001L)).thenReturn(other);

        ChatRequest request = new ChatRequest();
        request.setConversationId(15001L);
        request.setMessage("继续提问");

        assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AiChatErrorCode.AI_CHAT_FORBIDDEN.code());
    }

    @Test
    void chatShouldSaveFailedAssistantMessageWhenModelFails() {
        when(userContextService.currentUserId()).thenReturn(4L);
        mockConversationInsert(15001L);
        mockMessageInsert();
        when(messageMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AiMessagePO> page = invocation.getArgument(0);
            page.setRecords(List.of(userMessage(15101L, 15001L, 4L, "模型会失败吗")));
            return page;
        });
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenThrow(new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED, "AI 模型调用失败，请稍后重试"));

        ChatRequest request = new ChatRequest();
        request.setMessage("模型会失败吗");

        assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI 模型调用失败");

        ArgumentCaptor<AiMessagePO> messageCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(messageCaptor.capture());
        AiMessagePO failed = messageCaptor.getAllValues().get(1);
        assertThat(failed.getRole()).isEqualTo(AiMessageRole.ASSISTANT);
        assertThat(failed.getStatus()).isEqualTo(AiMessageStatus.FAILED);
        assertThat(failed.getErrorMessage()).contains("AI 模型调用失败");
    }

    private void mockConversationInsert(Long id) {
        doAnswer(invocation -> {
            AiConversationPO po = invocation.getArgument(0);
            po.setId(id);
            po.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0));
            po.setUpdateTime(LocalDateTime.of(2026, 5, 16, 10, 0));
            return 1;
        }).when(conversationMapper).insert(any(AiConversationPO.class));
    }

    private void mockMessageInsert() {
        AtomicLong id = new AtomicLong(15100L);
        doAnswer(invocation -> {
            AiMessagePO po = invocation.getArgument(0);
            po.setId(id.incrementAndGet());
            po.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0, (int) (po.getId() - 15100L)));
            po.setUpdateTime(po.getCreateTime());
            return 1;
        }).when(messageMapper).insert(any(AiMessagePO.class));
    }

    private AiConversationPO conversation(Long id, Long userId) {
        AiConversationPO po = new AiConversationPO();
        po.setId(id);
        po.setUserId(userId);
        po.setTitle("JWT 登录机制咨询");
        po.setScene(AiConversationScene.CHAT);
        return po;
    }

    private AiMessagePO userMessage(Long id, Long conversationId, Long userId, String content) {
        AiMessagePO po = new AiMessagePO();
        po.setId(id);
        po.setConversationId(conversationId);
        po.setUserId(userId);
        po.setRole(AiMessageRole.USER);
        po.setContent(content);
        po.setStatus(AiMessageStatus.SUCCESS);
        return po;
    }
}
