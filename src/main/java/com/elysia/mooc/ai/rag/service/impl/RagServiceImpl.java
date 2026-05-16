package com.elysia.mooc.ai.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.ai.chat.constants.AiChatConstants;
import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.chat.domain.vo.AiToolCallVO;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionMessage;
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
import com.elysia.mooc.ai.rag.service.RagService;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.constants.KnowledgeConstants;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** RAG 非流式问答服务实现。 */
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final UserContextService userContextService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final AiChatProperties chatProperties;
    private final AiChatClient aiChatClient;
    private final KnowledgeRetriever knowledgeRetriever;
    private final RagPromptBuilder promptBuilder;
    private final CitationAssembler citationAssembler;

    /**
     * 执行 RAG 问答。
     *
     * @param request RAG 问答请求
     * @return RAG 回复和引用来源
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public RagChatResult chat(RagChatRequest request) {
        RagChatRequest safeRequest = requireChatRequest(request);
        String question = resolveQuestion(safeRequest);
        LoginUser loginUser = userContextService.currentLoginUser();

        // 1. 会话与用户问题先落库，后续检索或模型失败时仍能追踪用户真实问题。
        AiConversationPO conversation = resolveConversation(safeRequest, loginUser, question);
        RagScope scope = resolveScope(safeRequest, conversation, loginUser);
        AiMessagePO userMessage = saveUserMessage(conversation.getId(), loginUser.getUserId(), question);

        try {
            List<RetrievedSegment> segments = searchAccessibleSegments(question, safeRequest, scope, loginUser);
            if (segments.isEmpty()) {
                return handleEmptyResult(conversation, scope, loginUser.getUserId());
            }

            List<AiSourceVO> citations = citationAssembler.buildCitations(segments);
            ChatCompletionResult modelResult = aiChatClient.complete(buildCompletionRequest(question, segments));
            AiMessagePO assistantMessage = saveAssistantSuccessMessage(
                    conversation.getId(),
                    loginUser.getUserId(),
                    modelResult.content(),
                    modelResult,
                    citationAssembler.toJson(citations));
            touchConversation(conversation.getId(), assistantMessage.getCreateTime(), scope);
            return toRagResult(conversation.getId(), assistantMessage, citations);
        } catch (BizException ex) {
            AiMessagePO failedMessage = saveAssistantFailedMessage(conversation.getId(), loginUser.getUserId(), ex.getMessage());
            touchConversation(conversation.getId(), failedMessage.getCreateTime(), scope);
            throw ex;
        } catch (RuntimeException ex) {
            AiMessagePO failedMessage = saveAssistantFailedMessage(
                    conversation.getId(),
                    loginUser.getUserId(),
                    RagErrorCode.RAG_MODEL_FAILED.message());
            touchConversation(conversation.getId(), failedMessage.getCreateTime(), scope);
            throw new BizException(RagErrorCode.RAG_MODEL_FAILED);
        }
    }

    /**
     * 执行 RAG 检索预览。
     *
     * @param request 检索请求
     * @return 检索来源
     */
    @Override
    public RagSearchResult search(RagSearchRequest request) {
        RagSearchRequest safeRequest = requireSearchRequest(request);
        LoginUser loginUser = userContextService.currentLoginUser();
        RagScope scope = validateScope(safeRequest.getKnowledgeBaseId(), safeRequest.getCourseId(), loginUser);
        List<RetrievedSegment> segments = searchAccessibleSegments(safeRequest.getQuery().trim(), safeRequest, scope, loginUser);
        List<AiSourceVO> sources = citationAssembler.buildCitations(segments);

        RagSearchResult result = new RagSearchResult();
        result.setQuery(safeRequest.getQuery().trim());
        result.setSources(sources);
        if (sources.isEmpty()) {
            result.setContent(RagConstants.EMPTY_ANSWER);
            result.setAnswer(RagConstants.EMPTY_ANSWER);
        }
        return result;
    }

    private RagChatRequest requireChatRequest(RagChatRequest request) {
        if (request == null || !StringUtils.hasText(resolveQuestion(request))) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "问题内容不能为空");
        }
        if (resolveQuestion(request).length() > 4000) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "问题内容不能超过4000个字符");
        }
        return request;
    }

    private RagSearchRequest requireSearchRequest(RagSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "检索文本不能为空");
        }
        return request;
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

    private AiConversationPO resolveConversation(RagChatRequest request, LoginUser loginUser, String question) {
        if (request.getConversationId() == null) {
            RagScope scope = validateScope(request.getKnowledgeBaseId(), request.getCourseId(), loginUser);
            return createConversation(loginUser.getUserId(), question, scope);
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

    private AiConversationPO createConversation(Long userId, String question, RagScope scope) {
        LocalDateTime now = LocalDateTime.now();
        AiConversationPO conversation = new AiConversationPO();
        conversation.setUserId(userId);
        conversation.setTitle(buildTitle(question));
        conversation.setScene(AiConversationScene.RAG);
        conversation.setKbId(scope.knowledgeBaseId());
        conversation.setCourseId(scope.courseId());
        conversation.setMemoryStrategy(RagConstants.RAG_MEMORY_STRATEGY);
        conversation.setLastMessageTime(now);
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

    private List<RetrievedSegment> searchAccessibleSegments(
            String query,
            RagSearchRequest request,
            RagScope scope,
            LoginUser loginUser) {
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setQuery(query);
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

    private RagChatResult handleEmptyResult(AiConversationPO conversation, RagScope scope, Long userId) {
        AiMessagePO assistantMessage = saveAssistantEmptyMessage(conversation.getId(), userId);
        touchConversation(conversation.getId(), assistantMessage.getCreateTime(), scope);
        return toRagResult(conversation.getId(), assistantMessage, Collections.emptyList());
    }

    private ChatCompletionRequest buildCompletionRequest(String question, List<RetrievedSegment> segments) {
        String prompt = promptBuilder.build(question, segments);
        List<ChatCompletionMessage> messages = StringUtils.hasText(chatProperties.getSystemPrompt())
                ? List.of(
                        new ChatCompletionMessage("system", chatProperties.getSystemPrompt().trim()),
                        new ChatCompletionMessage("user", prompt))
                : List.of(new ChatCompletionMessage("user", prompt));
        return new ChatCompletionRequest(chatProperties.getModel(), messages);
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

    private AiMessagePO saveAssistantSuccessMessage(
            Long conversationId,
            Long userId,
            String content,
            ChatCompletionResult result,
            String citationsJson) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent(StringUtils.hasText(content) ? content : "资料不足，暂时无法回答。");
        message.setStatus(AiMessageStatus.SUCCESS);
        message.setModelName(result.model());
        message.setPromptTokens(result.promptTokens());
        message.setCompletionTokens(result.completionTokens());
        message.setTotalTokens(result.totalTokens());
        message.setFinishReason(result.finishReason());
        message.setCitations(citationsJson);
        messageMapper.insert(message);
        return message;
    }

    private AiMessagePO saveAssistantEmptyMessage(Long conversationId, Long userId) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent(RagConstants.EMPTY_ANSWER);
        message.setStatus(AiMessageStatus.SUCCESS);
        message.setModelName(chatProperties.getModel());
        message.setPromptTokens(0);
        message.setCompletionTokens(0);
        message.setTotalTokens(0);
        message.setFinishReason("no_hit");
        message.setCitations("[]");
        messageMapper.insert(message);
        return message;
    }

    private AiMessagePO saveAssistantFailedMessage(Long conversationId, Long userId, String errorMessage) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent("RAG 问答失败，请稍后重试。");
        message.setStatus(AiMessageStatus.FAILED);
        message.setModelName(chatProperties.getModel());
        message.setFinishReason("error");
        message.setErrorMessage(abbreviate(errorMessage, 1000));
        message.setCitations("[]");
        messageMapper.insert(message);
        return message;
    }

    private void touchConversation(Long conversationId, LocalDateTime fallbackTime, RagScope scope) {
        AiConversationPO conversation = new AiConversationPO();
        conversation.setId(conversationId);
        conversation.setKbId(scope.knowledgeBaseId());
        conversation.setCourseId(scope.courseId());
        conversation.setLastMessageTime(fallbackTime == null ? LocalDateTime.now() : fallbackTime);
        conversationMapper.updateById(conversation);
    }

    private RagChatResult toRagResult(Long conversationId, AiMessagePO message, List<AiSourceVO> citations) {
        List<AiSourceVO> safeCitations = citations == null ? Collections.emptyList() : citations;
        RagChatResult vo = new RagChatResult();
        vo.setConversationId(conversationId);
        vo.setMessageId(message.getId());
        vo.setContent(message.getContent());
        vo.setAnswer(message.getContent());
        vo.setSources(safeCitations);
        vo.setCitations(safeCitations);
        vo.setToolCalls(Collections.<AiToolCallVO>emptyList());
        vo.setStatus(message.getStatus());
        vo.setModelName(message.getModelName());
        vo.setPromptTokens(message.getPromptTokens());
        vo.setCompletionTokens(message.getCompletionTokens());
        vo.setTotalTokens(message.getTotalTokens());
        vo.setFinishReason(message.getFinishReason());
        vo.setErrorMessage(message.getErrorMessage());
        return vo;
    }

    private String buildTitle(String message) {
        String text = message == null ? "新的 RAG 会话" : message.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(text)) {
            return "新的 RAG 会话";
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

    private record RagScope(Long knowledgeBaseId, Long courseId) {
    }
}
