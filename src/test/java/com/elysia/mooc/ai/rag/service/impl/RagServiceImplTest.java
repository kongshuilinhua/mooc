package com.elysia.mooc.ai.rag.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionRequest;
import com.elysia.mooc.ai.model.ChatCompletionResult;
import com.elysia.mooc.ai.rag.constants.RagConstants;
import com.elysia.mooc.ai.rag.constants.RagErrorCode;
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.domain.vo.RagChatResult;
import com.elysia.mooc.ai.rag.domain.vo.RagSearchResult;
import com.elysia.mooc.ai.rag.service.CitationAssembler;
import com.elysia.mooc.ai.rag.service.KnowledgeRetriever;
import com.elysia.mooc.ai.rag.service.RagPromptBuilder;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RAG 问答服务测试。 */
@ExtendWith(MockitoExtension.class)
class RagServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private AiConversationMapper conversationMapper;

    @Mock
    private AiMessageMapper messageMapper;

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private KnowledgeRetriever knowledgeRetriever;

    private RagServiceImpl ragService;

    @BeforeEach
    void setUp() {
        AiChatProperties properties = new AiChatProperties();
        properties.setModel("qwen-plus");
        properties.setSystemPrompt("你是学习助手");
        CitationAssembler citationAssembler = new CitationAssemblerImpl(new ObjectMapper());
        RagPromptBuilder promptBuilder = new RagPromptBuilderImpl(new RagProperties());
        ragService = new RagServiceImpl(
                userContextService,
                conversationMapper,
                messageMapper,
                knowledgeBaseMapper,
                properties,
                aiChatClient,
                knowledgeRetriever,
                promptBuilder,
                citationAssembler);
    }

    @Test
    void chatShouldReturnAnswerAndPersistCitations() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        when(knowledgeBaseMapper.selectBatchIds(List.of(12002L))).thenReturn(List.of(courseKnowledgeBase()));
        mockConversationInsert(16001L);
        mockMessageInsert();
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of(segment(12204L, 0.91D)));
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenReturn(new ChatCompletionResult("切片可以提升召回精度。", "qwen-plus", 100, 30, 130, "stop"));

        RagChatRequest request = new RagChatRequest();
        request.setMessage("RAG 中为什么要切片？");
        request.setKnowledgeBaseId(12002L);
        request.setCourseId(3003L);

        RagChatResult result = ragService.chat(request);

        assertThat(result.getConversationId()).isEqualTo(16001L);
        assertThat(result.getContent()).isEqualTo("切片可以提升召回精度。");
        assertThat(result.getAnswer()).isEqualTo(result.getContent());
        assertThat(result.getSources()).hasSize(1);
        assertThat(result.getSources().get(0).getSegmentId()).isEqualTo(12204L);
        assertThat(result.getModelName()).isEqualTo("qwen-plus");

        ArgumentCaptor<AiConversationPO> conversationCaptor = ArgumentCaptor.forClass(AiConversationPO.class);
        verify(conversationMapper).insert(conversationCaptor.capture());
        assertThat(conversationCaptor.getValue().getScene()).isEqualTo(AiConversationScene.RAG);
        assertThat(conversationCaptor.getValue().getMemoryStrategy()).isEqualTo(RagConstants.RAG_MEMORY_STRATEGY);

        ArgumentCaptor<AiMessagePO> messageCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(messageCaptor.capture());
        AiMessagePO assistant = messageCaptor.getAllValues().get(1);
        assertThat(assistant.getRole()).isEqualTo(AiMessageRole.ASSISTANT);
        assertThat(assistant.getCitations()).contains("\"segmentId\":12204");
    }

    @Test
    void chatShouldReturnEmptyAnswerWithoutCallingModelWhenNoSegments() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        mockConversationInsert(16001L);
        mockMessageInsert();
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of());

        RagChatRequest request = new RagChatRequest();
        request.setMessage("课程外问题");
        request.setKnowledgeBaseId(12002L);

        RagChatResult result = ragService.chat(request);

        assertThat(result.getContent()).isEqualTo(RagConstants.EMPTY_ANSWER);
        assertThat(result.getSources()).isEmpty();
        assertThat(result.getFinishReason()).isEqualTo("no_hit");
        verify(aiChatClient, never()).complete(any());
    }

    @Test
    void chatShouldRejectConversationOfOtherUser() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        AiConversationPO conversation = conversation(16001L, 99L);
        when(conversationMapper.selectById(16001L)).thenReturn(conversation);

        RagChatRequest request = new RagChatRequest();
        request.setConversationId(16001L);
        request.setMessage("继续问");

        assertThatThrownBy(() -> ragService.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(15003);
    }

    @Test
    void chatShouldSaveFailedAssistantWhenModelFails() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        when(knowledgeBaseMapper.selectBatchIds(List.of(12002L))).thenReturn(List.of(courseKnowledgeBase()));
        mockConversationInsert(16001L);
        mockMessageInsert();
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of(segment(12204L, 0.91D)));
        when(aiChatClient.complete(any(ChatCompletionRequest.class))).thenThrow(new RuntimeException("timeout"));

        RagChatRequest request = new RagChatRequest();
        request.setMessage("RAG 中为什么要切片？");
        request.setKnowledgeBaseId(12002L);

        assertThatThrownBy(() -> ragService.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(RagErrorCode.RAG_MODEL_FAILED.code());

        ArgumentCaptor<AiMessagePO> messageCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(messageCaptor.capture());
        AiMessagePO failed = messageCaptor.getAllValues().get(1);
        assertThat(failed.getStatus()).isEqualTo(AiMessageStatus.FAILED);
        assertThat(failed.getErrorMessage()).contains("AI 模型调用失败");
    }

    @Test
    void searchShouldRejectAdminKnowledgeBaseForStudent() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        KnowledgeBasePO adminKb = courseKnowledgeBase();
        adminKb.setScopeType(KnowledgeScopeType.ADMIN);
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(adminKb);

        RagSearchRequest request = new RagSearchRequest();
        request.setQuery("审核流程");
        request.setKnowledgeBaseId(12002L);

        assertThatThrownBy(() -> ragService.search(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(RagErrorCode.RAG_FORBIDDEN.code());
    }

    @Test
    void searchShouldReturnSources() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        when(knowledgeBaseMapper.selectBatchIds(List.of(12002L))).thenReturn(List.of(courseKnowledgeBase()));
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of(segment(12204L, 0.91D)));

        RagSearchRequest request = new RagSearchRequest();
        request.setQuery("文档切片");
        request.setKnowledgeBaseId(12002L);

        RagSearchResult result = ragService.search(request);

        assertThat(result.getQuery()).isEqualTo("文档切片");
        assertThat(result.getSources()).hasSize(1);
        assertThat(result.getSources().get(0).getPreview()).contains("上传文档");
    }

    private void mockConversationInsert(Long id) {
        doAnswer(invocation -> {
            AiConversationPO po = invocation.getArgument(0);
            po.setId(id);
            po.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0));
            po.setUpdateTime(po.getCreateTime());
            return 1;
        }).when(conversationMapper).insert(any(AiConversationPO.class));
    }

    private void mockMessageInsert() {
        AtomicLong id = new AtomicLong(16100L);
        doAnswer(invocation -> {
            AiMessagePO po = invocation.getArgument(0);
            po.setId(id.incrementAndGet());
            po.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0, (int) (po.getId() - 16100L)));
            po.setUpdateTime(po.getCreateTime());
            return 1;
        }).when(messageMapper).insert(any(AiMessagePO.class));
    }

    private LoginUser student() {
        return new LoginUser(4L, "student", List.of("STUDENT"), List.of("ai:chat"));
    }

    private KnowledgeBasePO courseKnowledgeBase() {
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(12002L);
        po.setName("RAG 课程知识库");
        po.setScopeType(KnowledgeScopeType.COURSE);
        po.setCourseId(3003L);
        po.setStatus(EnableStatus.ENABLED);
        return po;
    }

    private AiConversationPO conversation(Long id, Long userId) {
        AiConversationPO po = new AiConversationPO();
        po.setId(id);
        po.setUserId(userId);
        po.setScene(AiConversationScene.RAG);
        po.setKbId(12002L);
        po.setCourseId(3003L);
        return po;
    }

    private RetrievedSegment segment(Long segmentId, Double score) {
        return new RetrievedSegment(
                12002L,
                12102L,
                segmentId,
                3003L,
                "PDF",
                "文档切片",
                "上传文档后先解析成纯文本，再按长度和重叠策略切成多个 segment。",
                score,
                "上传文档后先解析成纯文本");
    }
}
