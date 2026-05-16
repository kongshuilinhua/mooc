package com.elysia.mooc.ai.stream.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.constants.AiChatConstants;
import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.ai.chat.domain.dto.ChatRequest;
import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionMessage;
import com.elysia.mooc.ai.model.ChatCompletionRequest;
import com.elysia.mooc.ai.model.ChatCompletionResult;
import com.elysia.mooc.ai.rag.constants.RagConstants;
import com.elysia.mooc.ai.rag.constants.RagErrorCode;
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.service.CitationAssembler;
import com.elysia.mooc.ai.rag.service.KnowledgeRetriever;
import com.elysia.mooc.ai.rag.service.RagPromptBuilder;
import com.elysia.mooc.ai.rag.service.impl.RetrievedSegment;
import com.elysia.mooc.ai.stream.constants.SseEventName;
import com.elysia.mooc.ai.stream.domain.vo.StreamCitationVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamDoneVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamErrorVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamMessageVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamStartVO;
import com.elysia.mooc.ai.stream.domain.vo.StreamToolCallVO;
import com.elysia.mooc.ai.stream.service.StreamingChatService;
import com.elysia.mooc.ai.stream.support.SseEmitterFactory;
import com.elysia.mooc.ai.stream.support.StreamMessageAccumulator;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.ToolOrchestrationService;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** day17 AI 流式响应服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatServiceImpl implements StreamingChatService {

    private static final int STREAM_CHUNK_SIZE = 24;
    private static final String CHAT_MEMORY_STRATEGY = "RECENT_N";

    private final UserContextService userContextService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final AiChatProperties chatProperties;
    private final AiChatClient aiChatClient;
    private final KnowledgeRetriever knowledgeRetriever;
    private final RagPromptBuilder promptBuilder;
    private final CitationAssembler citationAssembler;
    private final SseEmitterFactory emitterFactory;
    private final ToolOrchestrationService toolOrchestrationService;

    /**
     * 普通聊天流式生成。
     *
     * @param request 聊天请求
     * @return SSE 连接
     */
    @Override
    public SseEmitter streamChat(ChatRequest request) {
        ChatRequest safeRequest = requireChatRequest(request);
        LoginUser loginUser = userContextService.currentLoginUser();
        StreamRuntime runtime = createRuntime();
        emitterFactory.execute(() -> doStreamChat(safeRequest, loginUser, runtime));
        return runtime.emitter();
    }

    /**
     * RAG 问答流式生成。
     *
     * @param request RAG 请求
     * @return SSE 连接
     */
    @Override
    public SseEmitter streamRag(RagChatRequest request) {
        RagChatRequest safeRequest = requireRagRequest(request);
        LoginUser loginUser = userContextService.currentLoginUser();
        StreamRuntime runtime = createRuntime();
        emitterFactory.execute(() -> doStreamRag(safeRequest, loginUser, runtime));
        return runtime.emitter();
    }

    private StreamRuntime createRuntime() {
        AtomicReference<AiMessagePO> assistantRef = new AtomicReference<>();
        AtomicBoolean terminal = new AtomicBoolean(false);
        SseEmitter emitter = emitterFactory.create(() -> {
            if (terminal.compareAndSet(false, true)) {
                markInterrupted(assistantRef.get());
            }
        });
        return new StreamRuntime(emitter, assistantRef, terminal);
    }

    private void doStreamChat(ChatRequest request, LoginUser loginUser, StreamRuntime runtime) {
        AiConversationPO conversation = null;
        AiMessagePO assistant = null;
        try {
            Long userId = loginUser.getUserId();
            conversation = resolveChatConversation(request, userId);
            AiMessagePO userMessage = saveUserMessage(conversation.getId(), userId, request.getMessage().trim());
            assistant = saveAssistantStreamingMessage(conversation.getId(), userId, null);
            runtime.assistantRef().set(assistant);
            sendStart(runtime, conversation.getId(), assistant.getId(), AiConversationScene.CHAT);
            List<ToolCallResult> toolResults = toolOrchestrationService.planAndExecute(
                    request.getMessage(),
                    conversation.getId(),
                    userMessage.getId(),
                    loginUser);
            sendToolCalls(runtime, toolResults);

            ChatCompletionResult result = aiChatClient.complete(buildChatCompletionRequest(conversation.getId(), toolResults));
            StreamMessageAccumulator accumulator = sendContent(runtime, result.content());
            updateAssistantSuccess(assistant.getId(), accumulator.content(), result, null);
            touchConversation(conversation.getId(), assistant.getCreateTime(), null, request.getCourseId());
            sendDone(runtime, conversation.getId(), assistant.getId(), result, "stop");
        } catch (Exception ex) {
            handleStreamError(runtime, conversation, assistant, "AI 流式生成失败，请稍后重试", ex);
        }
    }

    private void doStreamRag(RagChatRequest request, LoginUser loginUser, StreamRuntime runtime) {
        AiConversationPO conversation = null;
        AiMessagePO assistant = null;
        RagScope scope = new RagScope(request.getKnowledgeBaseId(), request.getCourseId());
        try {
            String question = resolveQuestion(request);
            conversation = resolveRagConversation(request, loginUser, question);
            scope = resolveScope(request, conversation, loginUser);
            saveUserMessage(conversation.getId(), loginUser.getUserId(), question);
            assistant = saveAssistantStreamingMessage(conversation.getId(), loginUser.getUserId(), "[]");
            runtime.assistantRef().set(assistant);
            sendStart(runtime, conversation.getId(), assistant.getId(), AiConversationScene.RAG);

            List<RetrievedSegment> segments = searchAccessibleSegments(question, request, scope, loginUser);
            List<AiSourceVO> citations = citationAssembler.buildCitations(segments);
            emitterFactory.send(runtime.emitter(), SseEventName.CITATION, new StreamCitationVO(citations));
            if (segments.isEmpty()) {
                StreamMessageAccumulator accumulator = sendContent(runtime, RagConstants.EMPTY_ANSWER);
                ChatCompletionResult noHit = new ChatCompletionResult(
                        accumulator.content(), chatProperties.getModel(), 0, 0, 0, "no_hit");
                updateAssistantSuccess(assistant.getId(), accumulator.content(), noHit, "[]");
                touchConversation(conversation.getId(), assistant.getCreateTime(), scope.knowledgeBaseId(), scope.courseId());
                sendDone(runtime, conversation.getId(), assistant.getId(), noHit, "no_hit");
                return;
            }

            ChatCompletionResult result = aiChatClient.complete(buildRagCompletionRequest(question, segments));
            StreamMessageAccumulator accumulator = sendContent(runtime, result.content());
            updateAssistantSuccess(assistant.getId(), accumulator.content(), result, citationAssembler.toJson(citations));
            touchConversation(conversation.getId(), assistant.getCreateTime(), scope.knowledgeBaseId(), scope.courseId());
            sendDone(runtime, conversation.getId(), assistant.getId(), result, "stop");
        } catch (Exception ex) {
            handleStreamError(runtime, conversation, assistant, "RAG 流式生成失败，请稍后重试", ex);
        }
    }

    private ChatRequest requireChatRequest(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "聊天内容不能为空");
        }
        if (request.getMessage().trim().length() > 4000) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "聊天内容不能超过4000个字符");
        }
        return request;
    }

    private RagChatRequest requireRagRequest(RagChatRequest request) {
        if (request == null || !StringUtils.hasText(resolveQuestion(request))) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "问题内容不能为空");
        }
        if (resolveQuestion(request).length() > 4000) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "问题内容不能超过4000个字符");
        }
        return request;
    }

    private AiConversationPO resolveChatConversation(ChatRequest request, Long userId) {
        if (request.getConversationId() == null) {
            return createChatConversation(request, userId);
        }
        AiConversationPO conversation = conversationMapper.selectById(request.getConversationId());
        if (conversation == null) {
            throw new BizException(AiChatErrorCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!userId.equals(conversation.getUserId())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_FORBIDDEN);
        }
        if (conversation.getScene() != AiConversationScene.CHAT) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "只能在普通聊天会话中继续流式聊天");
        }
        return conversation;
    }

    private AiConversationPO createChatConversation(ChatRequest request, Long userId) {
        AiConversationPO conversation = new AiConversationPO();
        conversation.setUserId(userId);
        conversation.setTitle(buildTitle(request.getMessage(), "新的 AI 会话"));
        conversation.setScene(AiConversationScene.CHAT);
        conversation.setCourseId(request.getCourseId());
        conversation.setMemoryStrategy(CHAT_MEMORY_STRATEGY);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation;
    }

    private AiConversationPO resolveRagConversation(RagChatRequest request, LoginUser loginUser, String question) {
        if (request.getConversationId() == null) {
            RagScope scope = validateScope(request.getKnowledgeBaseId(), request.getCourseId(), loginUser);
            return createRagConversation(loginUser.getUserId(), question, scope);
        }
        AiConversationPO conversation = conversationMapper.selectById(request.getConversationId());
        if (conversation == null) {
            throw new BizException(AiChatErrorCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!loginUser.getUserId().equals(conversation.getUserId())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_FORBIDDEN);
        }
        if (conversation.getScene() != AiConversationScene.RAG) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "只能在 RAG 会话中继续问答");
        }
        return conversation;
    }

    private AiConversationPO createRagConversation(Long userId, String question, RagScope scope) {
        AiConversationPO conversation = new AiConversationPO();
        conversation.setUserId(userId);
        conversation.setTitle(buildTitle(question, "新的 RAG 会话"));
        conversation.setScene(AiConversationScene.RAG);
        conversation.setKbId(scope.knowledgeBaseId());
        conversation.setCourseId(scope.courseId());
        conversation.setMemoryStrategy(RagConstants.RAG_MEMORY_STRATEGY);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversationMapper.insert(conversation);
        return conversation;
    }

    private RagScope resolveScope(RagChatRequest request, AiConversationPO conversation, LoginUser loginUser) {
        Long requestedKbId = request.getKnowledgeBaseId() == null ? conversation.getKbId() : request.getKnowledgeBaseId();
        Long requestedCourseId = request.getCourseId() == null ? conversation.getCourseId() : request.getCourseId();
        if (conversation.getKbId() != null && request.getKnowledgeBaseId() != null
                && !conversation.getKbId().equals(request.getKnowledgeBaseId())) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "不能在同一会话中切换知识库");
        }
        if (conversation.getCourseId() != null && request.getCourseId() != null
                && !conversation.getCourseId().equals(request.getCourseId())) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "不能在同一会话中切换课程上下文");
        }
        return validateScope(requestedKbId, requestedCourseId, loginUser);
    }

    private RagScope validateScope(Long knowledgeBaseId, Long courseId, LoginUser loginUser) {
        if (knowledgeBaseId == null) {
            return new RagScope(null, courseId);
        }
        KnowledgeBasePO knowledgeBase = knowledgeBaseMapper.selectById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "知识库不存在");
        }
        if (knowledgeBase.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "知识库已停用");
        }
        if (knowledgeBase.getScopeType() == KnowledgeScopeType.ADMIN && !canManageKnowledge(loginUser)) {
            throw new BizException(RagErrorCode.RAG_FORBIDDEN, "没有权限检索后台知识库");
        }
        Long resolvedCourseId = courseId == null ? knowledgeBase.getCourseId() : courseId;
        if (knowledgeBase.getCourseId() != null && resolvedCourseId != null
                && !knowledgeBase.getCourseId().equals(resolvedCourseId)) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "知识库与课程上下文不一致");
        }
        return new RagScope(knowledgeBase.getId(), resolvedCourseId);
    }

    private List<RetrievedSegment> searchAccessibleSegments(
            String question,
            RagChatRequest request,
            RagScope scope,
            LoginUser loginUser) {
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setQuery(question);
        searchRequest.setKnowledgeBaseId(scope.knowledgeBaseId());
        searchRequest.setCourseId(scope.courseId());
        searchRequest.setTopK(request.getTopK());
        return filterAccessibleSegments(knowledgeRetriever.searchSegments(searchRequest), loginUser);
    }

    private List<RetrievedSegment> filterAccessibleSegments(List<RetrievedSegment> segments, LoginUser loginUser) {
        if (segments == null || segments.isEmpty()) {
            return List.of();
        }
        List<Long> kbIds = segments.stream()
                .map(RetrievedSegment::kbId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (kbIds.isEmpty()) {
            return List.of();
        }
        Map<Long, KnowledgeBasePO> baseMap = knowledgeBaseMapper.selectBatchIds(kbIds).stream()
                .collect(Collectors.toMap(KnowledgeBasePO::getId, Function.identity(), (left, right) -> left));
        return segments.stream()
                .filter(segment -> canReadKnowledgeBase(baseMap.get(segment.kbId()), loginUser))
                .toList();
    }

    private boolean canReadKnowledgeBase(KnowledgeBasePO knowledgeBase, LoginUser loginUser) {
        if (knowledgeBase == null || knowledgeBase.getStatus() != EnableStatus.ENABLED) {
            return false;
        }
        return knowledgeBase.getScopeType() != KnowledgeScopeType.ADMIN || canManageKnowledge(loginUser);
    }

    private boolean canManageKnowledge(LoginUser loginUser) {
        return hasRole(loginUser, KnowledgeConstants.ROLE_ADMIN)
                || hasPermission(loginUser, KnowledgeConstants.PERMISSION_KNOWLEDGE_MANAGE);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        List<String> roles = loginUser.getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        List<String> permissions = loginUser.getPermissions();
        return permissions != null && permissions.contains(permissionCode);
    }

    private AiMessagePO saveUserMessage(Long conversationId, Long userId, String content) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.USER);
        message.setContent(content);
        message.setStatus(AiMessageStatus.SUCCESS);
        messageMapper.insert(message);
        return message;
    }

    private AiMessagePO saveAssistantStreamingMessage(Long conversationId, Long userId, String citationsJson) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent("");
        message.setStatus(AiMessageStatus.STREAMING);
        message.setModelName(chatProperties.getModel());
        message.setCitations(citationsJson);
        messageMapper.insert(message);
        return message;
    }

    private void updateAssistantSuccess(
            Long messageId,
            String content,
            ChatCompletionResult result,
            String citationsJson) {
        AiMessagePO message = new AiMessagePO();
        message.setId(messageId);
        message.setContent(StringUtils.hasText(content) ? content : "资料不足，暂时无法回答。");
        message.setStatus(AiMessageStatus.SUCCESS);
        message.setModelName(result.model());
        message.setPromptTokens(result.promptTokens());
        message.setCompletionTokens(result.completionTokens());
        message.setTotalTokens(result.totalTokens());
        message.setFinishReason(result.finishReason());
        message.setCitations(citationsJson);
        messageMapper.updateById(message);
    }

    private void updateAssistantFailed(AiMessagePO assistant, String partialContent, String errorMessage) {
        if (assistant == null || assistant.getId() == null) {
            return;
        }
        AiMessagePO message = new AiMessagePO();
        message.setId(assistant.getId());
        message.setContent(StringUtils.hasText(partialContent) ? partialContent : "AI 流式生成失败，请稍后重试。");
        message.setStatus(AiMessageStatus.FAILED);
        message.setModelName(chatProperties.getModel());
        message.setFinishReason("error");
        message.setErrorMessage(abbreviate(errorMessage, 1000));
        messageMapper.updateById(message);
    }

    private void markInterrupted(AiMessagePO assistant) {
        if (assistant == null || assistant.getStatus() != AiMessageStatus.STREAMING) {
            return;
        }
        updateAssistantFailed(assistant, assistant.getContent(), "流式连接已断开");
    }

    private ChatCompletionRequest buildChatCompletionRequest(Long conversationId, List<ToolCallResult> toolResults) {
        List<ChatCompletionMessage> messages = new ArrayList<>();
        if (StringUtils.hasText(chatProperties.getSystemPrompt())) {
            messages.add(new ChatCompletionMessage("system", chatProperties.getSystemPrompt().trim()));
        }
        for (AiMessagePO message : recentSuccessMessages(conversationId)) {
            messages.add(new ChatCompletionMessage(toOpenAiRole(message.getRole()), message.getContent()));
        }
        String toolContext = toolOrchestrationService.buildToolContext(toolResults);
        if (StringUtils.hasText(toolContext)) {
            messages.add(new ChatCompletionMessage("system", toolContext));
        }
        return new ChatCompletionRequest(chatProperties.getModel(), messages);
    }

    private ChatCompletionRequest buildRagCompletionRequest(String question, List<RetrievedSegment> segments) {
        String prompt = promptBuilder.build(question, segments);
        List<ChatCompletionMessage> messages = StringUtils.hasText(chatProperties.getSystemPrompt())
                ? List.of(
                        new ChatCompletionMessage("system", chatProperties.getSystemPrompt().trim()),
                        new ChatCompletionMessage("user", prompt))
                : List.of(new ChatCompletionMessage("user", prompt));
        return new ChatCompletionRequest(chatProperties.getModel(), messages);
    }

    private List<AiMessagePO> recentSuccessMessages(Long conversationId) {
        int limit = chatProperties.getHistoryLimit() <= 0
                ? AiChatConstants.DEFAULT_CONTEXT_MESSAGE_LIMIT
                : Math.min(chatProperties.getHistoryLimit(), 50);
        Page<AiMessagePO> page = new Page<>(1, limit);
        Page<AiMessagePO> result = messageMapper.selectPage(page, new LambdaQueryWrapper<AiMessagePO>()
                .eq(AiMessagePO::getConversationId, conversationId)
                .eq(AiMessagePO::getStatus, AiMessageStatus.SUCCESS)
                .in(AiMessagePO::getRole, List.of(AiMessageRole.USER, AiMessageRole.ASSISTANT))
                .orderByDesc(AiMessagePO::getCreateTime)
                .orderByDesc(AiMessagePO::getId));
        List<AiMessagePO> records = new ArrayList<>(result.getRecords());
        Collections.reverse(records);
        return records;
    }

    private StreamMessageAccumulator sendContent(StreamRuntime runtime, String content) {
        StreamMessageAccumulator accumulator = new StreamMessageAccumulator();
        for (String chunk : splitContent(content)) {
            accumulator.append(chunk);
            emitterFactory.send(runtime.emitter(), SseEventName.MESSAGE, new StreamMessageVO(chunk));
        }
        return accumulator;
    }

    private void sendStart(StreamRuntime runtime, Long conversationId, Long messageId, AiConversationScene scene) {
        emitterFactory.send(runtime.emitter(), SseEventName.START,
                new StreamStartVO(conversationId, messageId, scene.getValue()));
    }

    private void sendToolCalls(StreamRuntime runtime, List<ToolCallResult> toolResults) {
        if (toolResults == null || toolResults.isEmpty()) {
            return;
        }
        for (ToolCallResult result : toolResults) {
            StreamToolCallVO vo = new StreamToolCallVO();
            vo.setToolName(result.getToolName());
            vo.setArguments(result.getArguments());
            vo.setSuccess(Boolean.TRUE.equals(result.getSuccess()));
            vo.setResultSummary(result.getResultSummary());
            vo.setLatencyMs(result.getLatencyMs());
            vo.setErrorMessage(result.getErrorMessage());
            emitterFactory.send(runtime.emitter(), SseEventName.TOOL_CALL, vo);
        }
    }

    private void sendDone(
            StreamRuntime runtime,
            Long conversationId,
            Long messageId,
            ChatCompletionResult result,
            String fallbackFinishReason) {
        StreamDoneVO done = new StreamDoneVO();
        done.setConversationId(conversationId);
        done.setMessageId(messageId);
        done.setStatus(AiMessageStatus.SUCCESS);
        done.setFinishReason(StringUtils.hasText(result.finishReason()) ? result.finishReason() : fallbackFinishReason);
        done.setPromptTokens(result.promptTokens());
        done.setCompletionTokens(result.completionTokens());
        done.setTotalTokens(result.totalTokens());
        runtime.terminal().set(true);
        emitterFactory.send(runtime.emitter(), SseEventName.DONE, done);
        emitterFactory.complete(runtime.emitter());
    }

    private void handleStreamError(
            StreamRuntime runtime,
            AiConversationPO conversation,
            AiMessagePO assistant,
            String userMessage,
            Exception ex) {
        log.warn("AI 流式响应失败，conversationId={}, messageId={}",
                conversation == null ? null : conversation.getId(),
                assistant == null ? null : assistant.getId(),
                ex);
        if (!runtime.terminal().compareAndSet(false, true)) {
            return;
        }
        String message = ex instanceof BizException bizException && StringUtils.hasText(bizException.getMessage())
                ? bizException.getMessage()
                : userMessage;
        updateAssistantFailed(assistant, null, message);
        StreamErrorVO error = new StreamErrorVO();
        error.setConversationId(conversation == null ? null : conversation.getId());
        error.setMessageId(assistant == null ? null : assistant.getId());
        error.setStatus(AiMessageStatus.FAILED);
        error.setErrorMessage(message);
        try {
            emitterFactory.send(runtime.emitter(), SseEventName.ERROR, error);
        } catch (RuntimeException sendError) {
            log.warn("发送 SSE 错误事件失败", sendError);
        } finally {
            emitterFactory.complete(runtime.emitter());
        }
    }

    private void touchConversation(Long conversationId, LocalDateTime fallbackTime, Long kbId, Long courseId) {
        AiConversationPO conversation = new AiConversationPO();
        conversation.setId(conversationId);
        conversation.setKbId(kbId);
        conversation.setCourseId(courseId);
        conversation.setLastMessageTime(fallbackTime == null ? LocalDateTime.now() : fallbackTime);
        conversationMapper.updateById(conversation);
    }

    private List<String> splitContent(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of("资料不足，暂时无法回答。");
        }
        List<String> chunks = new ArrayList<>();
        String text = content.trim();
        for (int start = 0; start < text.length(); start += STREAM_CHUNK_SIZE) {
            chunks.add(text.substring(start, Math.min(text.length(), start + STREAM_CHUNK_SIZE)));
        }
        return chunks;
    }

    private String resolveQuestion(RagChatRequest request) {
        if (request == null) {
            return "";
        }
        if (StringUtils.hasText(request.getMessage())) {
            return request.getMessage().trim();
        }
        return StringUtils.hasText(request.getQuestion()) ? request.getQuestion().trim() : "";
    }

    private String toOpenAiRole(AiMessageRole role) {
        if (role == AiMessageRole.USER) {
            return "user";
        }
        if (role == AiMessageRole.ASSISTANT) {
            return "assistant";
        }
        if (role == AiMessageRole.SYSTEM) {
            return "system";
        }
        return "tool";
    }

    private String buildTitle(String message, String fallback) {
        String text = message == null ? fallback : message.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(text)) {
            return fallback;
        }
        return abbreviate(text, AiChatConstants.TITLE_MAX_LENGTH);
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private record StreamRuntime(
            SseEmitter emitter,
            AtomicReference<AiMessagePO> assistantRef,
            AtomicBoolean terminal) {
    }

    private record RagScope(Long knowledgeBaseId, Long courseId) {
    }
}
