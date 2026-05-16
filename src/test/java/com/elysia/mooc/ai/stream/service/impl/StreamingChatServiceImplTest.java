package com.elysia.mooc.ai.stream.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
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
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.service.CitationAssembler;
import com.elysia.mooc.ai.rag.service.KnowledgeRetriever;
import com.elysia.mooc.ai.rag.service.RagPromptBuilder;
import com.elysia.mooc.ai.rag.service.impl.CitationAssemblerImpl;
import com.elysia.mooc.ai.rag.service.impl.RagPromptBuilderImpl;
import com.elysia.mooc.ai.rag.service.impl.RagProperties;
import com.elysia.mooc.ai.rag.service.impl.RetrievedSegment;
import com.elysia.mooc.ai.stream.constants.SseEventName;
import com.elysia.mooc.ai.stream.domain.vo.StreamCitationVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamDoneVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamErrorVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamMessageVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamStartVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamToolCallVO;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.ToolOrchestrationService;
import com.elysia.mooc.ai.stream.support.SseEmitterFactory;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** day17 流式响应服务测试。 */
@ExtendWith(MockitoExtension.class)
class StreamingChatServiceImplTest {

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

    @Mock
    private SseEmitterFactory emitterFactory;

    @Mock
    private ToolOrchestrationService toolOrchestrationService;

    private final SseEmitter emitter = new SseEmitter(1000L);
    private final List<SentEvent> events = new ArrayList<>();
    private StreamingChatServiceImpl streamingService;

    @BeforeEach
    void setUp() {
        AiChatProperties chatProperties = new AiChatProperties();
        chatProperties.setModel("qwen-plus");
        chatProperties.setSystemPrompt("你是学习助手");
        chatProperties.setHistoryLimit(10);
        CitationAssembler citationAssembler = new CitationAssemblerImpl(new ObjectMapper());
        RagPromptBuilder promptBuilder = new RagPromptBuilderImpl(new RagProperties());
        streamingService = new StreamingChatServiceImpl(
                userContextService,
                conversationMapper,
                messageMapper,
                knowledgeBaseMapper,
                chatProperties,
                aiChatClient,
                knowledgeRetriever,
                promptBuilder,
                citationAssembler,
                emitterFactory,
                toolOrchestrationService);
        when(emitterFactory.create(any())).thenReturn(emitter);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(emitterFactory).execute(any(Runnable.class));
        doAnswer(invocation -> {
            events.add(new SentEvent(invocation.getArgument(1), invocation.getArgument(2)));
            return null;
        }).when(emitterFactory).send(any(), any(), any());
    }

    @Test
    void streamChatShouldSendStartMessageAndDone() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        mockConversationInsert(15001L);
        mockMessageInsert(15100L);
        when(messageMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AiMessagePO> page = invocation.getArgument(0);
            page.setRecords(List.of(userMessage(15101L, 15001L, 4L, "解释 Java 接口")));
            return page;
        });
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenReturn(new ChatCompletionResult("Java 接口定义一组行为规范。", "qwen-plus", 20, 10, 30, "stop"));

        ChatRequest request = new ChatRequest();
        request.setMessage("解释 Java 接口");

        SseEmitter result = streamingService.streamChat(request);

        assertThat(result).isSameAs(emitter);
        assertThat(events).extracting(SentEvent::eventName)
                .containsSubsequence(SseEventName.START, SseEventName.MESSAGE, SseEventName.DONE);
        assertThat(events.stream().filter(event -> event.eventName() == SseEventName.START)
                .map(event -> (StreamStartVO) event.data()).findFirst().orElseThrow().getMessageId()).isEqualTo(15102L);
        assertThat(events.stream().filter(event -> event.eventName() == SseEventName.MESSAGE)
                .map(event -> (StreamMessageVO) event.data()).map(StreamMessageVO::getContent).toList())
                .contains("Java 接口定义一组行为规范。");

        ArgumentCaptor<AiMessagePO> insertCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper, org.mockito.Mockito.times(2)).insert(insertCaptor.capture());
        assertThat(insertCaptor.getAllValues().get(1).getStatus()).isEqualTo(AiMessageStatus.STREAMING);

        ArgumentCaptor<AiMessagePO> updateCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getStatus()).isEqualTo(AiMessageStatus.SUCCESS);
        assertThat(updateCaptor.getValue().getContent()).isEqualTo("Java 接口定义一组行为规范。");
    }

    @Test
    void streamChatShouldSendToolCallEventWhenToolTriggered() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        mockConversationInsert(15001L);
        mockMessageInsert(15100L);
        when(toolOrchestrationService.planAndExecute(any(), any(), any(), any()))
                .thenReturn(List.of(ToolCallResult.builder()
                        .toolName("CourseSearchTool")
                        .arguments(Map.of("keyword", "Java"))
                        .success(true)
                        .resultSummary("找到 1 门已发布课程：Java 入门")
                        .latencyMs(42L)
                        .build()));
        when(toolOrchestrationService.buildToolContext(any())).thenReturn("工具摘要：Java 入门");
        when(messageMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AiMessagePO> page = invocation.getArgument(0);
            page.setRecords(List.of(userMessage(15101L, 15001L, 4L, "帮我找 Java 入门课程")));
            return page;
        });
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenReturn(new ChatCompletionResult("推荐 Java 入门课程。", "qwen-plus", 20, 10, 30, "stop"));

        ChatRequest request = new ChatRequest();
        request.setMessage("帮我找 Java 入门课程");
        streamingService.streamChat(request);

        assertThat(events).extracting(SentEvent::eventName)
                .containsSubsequence(SseEventName.START, SseEventName.TOOL_CALL, SseEventName.MESSAGE, SseEventName.DONE);
        StreamToolCallVO toolCall = events.stream()
                .filter(event -> event.eventName() == SseEventName.TOOL_CALL)
                .map(event -> (StreamToolCallVO) event.data())
                .findFirst()
                .orElseThrow();
        assertThat(toolCall.getToolName()).isEqualTo("CourseSearchTool");
        assertThat(toolCall.getResultSummary()).contains("Java 入门");
    }

    @Test
    void streamRagShouldSendCitationMessageAndDone() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        when(knowledgeBaseMapper.selectBatchIds(List.of(12002L))).thenReturn(List.of(courseKnowledgeBase()));
        mockConversationInsert(16001L);
        mockMessageInsert(16100L);
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of(segment(12204L, 0.91D)));
        when(aiChatClient.complete(any(ChatCompletionRequest.class)))
                .thenReturn(new ChatCompletionResult("切片可以提升召回精度。", "qwen-plus", 100, 30, 130, "stop"));

        RagChatRequest request = new RagChatRequest();
        request.setMessage("RAG 为什么要切片？");
        request.setKnowledgeBaseId(12002L);
        request.setTopK(5);

        streamingService.streamRag(request);

        assertThat(events).extracting(SentEvent::eventName)
                .containsSubsequence(SseEventName.START, SseEventName.CITATION, SseEventName.MESSAGE, SseEventName.DONE);
        StreamCitationVO citation = events.stream()
                .filter(event -> event.eventName() == SseEventName.CITATION)
                .map(event -> (StreamCitationVO) event.data())
                .findFirst()
                .orElseThrow();
        assertThat(citation.getSources()).hasSize(1);
        assertThat(citation.getSources().get(0).getSegmentId()).isEqualTo(12204L);
        StreamDoneVO done = events.stream()
                .filter(event -> event.eventName() == SseEventName.DONE)
                .map(event -> (StreamDoneVO) event.data())
                .findFirst()
                .orElseThrow();
        assertThat(done.getFinishReason()).isEqualTo("stop");

        ArgumentCaptor<AiMessagePO> updateCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getCitations()).contains("\"segmentId\":12204");
    }

    @Test
    void streamRagShouldReturnNoHitWithoutCallingModel() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(knowledgeBaseMapper.selectById(12002L)).thenReturn(courseKnowledgeBase());
        mockConversationInsert(16001L);
        mockMessageInsert(16100L);
        when(knowledgeRetriever.searchSegments(any())).thenReturn(List.of());

        RagChatRequest request = new RagChatRequest();
        request.setMessage("无关问题");
        request.setKnowledgeBaseId(12002L);

        streamingService.streamRag(request);

        assertThat(events).extracting(SentEvent::eventName)
                .containsSubsequence(SseEventName.CITATION, SseEventName.MESSAGE, SseEventName.DONE);
        assertThat(events.stream().filter(event -> event.eventName() == SseEventName.MESSAGE)
                .map(event -> (StreamMessageVO) event.data()).map(StreamMessageVO::getContent).toList())
                .contains(RagConstants.EMPTY_ANSWER);
        verify(aiChatClient, never()).complete(any());
    }

    @Test
    void streamChatShouldMarkFailedAndSendErrorWhenModelFails() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        mockConversationInsert(15001L);
        mockMessageInsert(15100L);
        when(messageMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<AiMessagePO> page = invocation.getArgument(0);
            page.setRecords(List.of(userMessage(15101L, 15001L, 4L, "模型会失败吗")));
            return page;
        });
        when(aiChatClient.complete(any(ChatCompletionRequest.class))).thenThrow(new RuntimeException("timeout"));

        ChatRequest request = new ChatRequest();
        request.setMessage("模型会失败吗");

        streamingService.streamChat(request);

        assertThat(events).extracting(SentEvent::eventName).contains(SseEventName.ERROR);
        StreamErrorVO error = events.stream()
                .filter(event -> event.eventName() == SseEventName.ERROR)
                .map(event -> (StreamErrorVO) event.data())
                .findFirst()
                .orElseThrow();
        assertThat(error.getStatus()).isEqualTo(AiMessageStatus.FAILED);

        ArgumentCaptor<AiMessagePO> updateCaptor = ArgumentCaptor.forClass(AiMessagePO.class);
        verify(messageMapper).updateById(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getStatus()).isEqualTo(AiMessageStatus.FAILED);
        assertThat(updateCaptor.getValue().getErrorMessage()).contains("AI 流式生成失败");
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

    private void mockMessageInsert(Long startId) {
        AtomicLong id = new AtomicLong(startId);
        doAnswer(invocation -> {
            AiMessagePO po = invocation.getArgument(0);
            po.setId(id.incrementAndGet());
            po.setCreateTime(LocalDateTime.of(2026, 5, 16, 10, 0, (int) (po.getId() - startId)));
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

    private record SentEvent(SseEventName eventName, Object data) {
    }
}
