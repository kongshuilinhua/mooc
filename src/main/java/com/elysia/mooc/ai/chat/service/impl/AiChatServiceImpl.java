package com.elysia.mooc.ai.chat.service.impl;

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
import com.elysia.mooc.ai.chat.domain.vo.AiToolCallVO;
import com.elysia.mooc.ai.chat.domain.vo.ChatResultVO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.chat.service.AiChatService;
import com.elysia.mooc.ai.model.AiChatClient;
import com.elysia.mooc.ai.model.AiChatProperties;
import com.elysia.mooc.ai.model.ChatCompletionMessage;
import com.elysia.mooc.ai.model.ChatCompletionRequest;
import com.elysia.mooc.ai.model.ChatCompletionResult;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.ToolOrchestrationService;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** AI 普通聊天服务实现。 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final UserContextService userContextService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiChatProperties chatProperties;
    private final AiChatClient aiChatClient;
    private final ToolOrchestrationService toolOrchestrationService;

    /**
     * 发送普通聊天消息。
     *
     * @param request 聊天请求
     * @return AI 回复结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BizException.class)
    public ChatResultVO chat(ChatRequest request) {
        ChatRequest safeRequest = requireRequest(request);
        LoginUser loginUser = userContextService.currentLoginUser();
        Long userId = loginUser.getUserId();

        // 1. 会话和用户消息先落库，确保模型失败时也能留下用户可追踪的上下文。
        AiConversationPO conversation = resolveConversation(safeRequest, userId);
        AiMessagePO userMessage = saveUserMessage(conversation.getId(), userId, safeRequest.getMessage().trim());
        List<ToolCallResult> toolResults = toolOrchestrationService.planAndExecute(
                safeRequest.getMessage(),
                conversation.getId(),
                userMessage.getId(),
                loginUser);

        try {
            ChatCompletionResult result = aiChatClient.complete(
                    buildCompletionRequest(conversation.getId(), toolResults));
            AiMessagePO assistantMessage = saveAssistantSuccessMessage(conversation.getId(), userId, result);
            touchConversation(conversation.getId(), assistantMessage.getCreateTime());
            return toChatResult(conversation.getId(), assistantMessage, toolResults);
        } catch (BizException ex) {
            // 2. 模型失败不能抹掉用户消息，保存失败助手消息后再把中文错误交给全局异常处理。
            AiMessagePO failedMessage = saveAssistantFailedMessage(
                    conversation.getId(), userId, ex.getMessage(), safeRequest.getMessage());
            touchConversation(conversation.getId(), failedMessage.getCreateTime());
            throw ex;
        } catch (RuntimeException ex) {
            AiMessagePO failedMessage = saveAssistantFailedMessage(
                    conversation.getId(), userId, AiChatErrorCode.AI_CHAT_MODEL_FAILED.message(), safeRequest.getMessage());
            touchConversation(conversation.getId(), failedMessage.getCreateTime());
            throw new BizException(AiChatErrorCode.AI_CHAT_MODEL_FAILED);
        }
    }

    private ChatRequest requireRequest(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "聊天内容不能为空");
        }
        if (request.getMessage().trim().length() > 4000) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "聊天内容不能超过4000个字符");
        }
        return request;
    }

    private AiConversationPO resolveConversation(ChatRequest request, Long userId) {
        if (request.getConversationId() == null) {
            return createConversation(request, userId);
        }
        AiConversationPO conversation = conversationMapper.selectById(request.getConversationId());
        if (conversation == null) {
            throw new BizException(AiChatErrorCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!userId.equals(conversation.getUserId())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_FORBIDDEN);
        }
        return conversation;
    }

    private AiConversationPO createConversation(ChatRequest request, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        AiConversationPO conversation = new AiConversationPO();
        conversation.setUserId(userId);
        conversation.setTitle(buildTitle(request.getMessage()));
        conversation.setScene(AiConversationScene.CHAT);
        conversation.setCourseId(request.getCourseId());
        conversation.setMemoryStrategy("RECENT_N");
        conversation.setLastMessageTime(now);
        conversationMapper.insert(conversation);
        return conversation;
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

    private AiMessagePO saveAssistantSuccessMessage(Long conversationId, Long userId, ChatCompletionResult result) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent(result.content());
        message.setStatus(AiMessageStatus.SUCCESS);
        message.setModelName(result.model());
        message.setPromptTokens(result.promptTokens());
        message.setCompletionTokens(result.completionTokens());
        message.setTotalTokens(result.totalTokens());
        message.setFinishReason(result.finishReason());
        messageMapper.insert(message);
        return message;
    }

    private AiMessagePO saveAssistantFailedMessage(
            Long conversationId,
            Long userId,
            String errorMessage,
            String fallbackQuestion) {
        AiMessagePO message = new AiMessagePO();
        message.setConversationId(conversationId);
        message.setUserId(userId);
        message.setRole(AiMessageRole.ASSISTANT);
        message.setContent("AI 生成失败，请稍后重试。");
        message.setStatus(AiMessageStatus.FAILED);
        message.setModelName(chatProperties.getModel());
        message.setErrorMessage(abbreviate(errorMessage, 1000));
        message.setFinishReason("error");
        messageMapper.insert(message);
        return message;
    }

    private ChatCompletionRequest buildCompletionRequest(Long conversationId, List<ToolCallResult> toolResults) {
        List<ChatCompletionMessage> messages = new ArrayList<>();
        if (StringUtils.hasText(chatProperties.getSystemPrompt())) {
            messages.add(new ChatCompletionMessage("system", chatProperties.getSystemPrompt().trim()));
        }
        List<AiMessagePO> history = recentSuccessMessages(conversationId);
        for (AiMessagePO message : history) {
            messages.add(new ChatCompletionMessage(toOpenAiRole(message.getRole()), message.getContent()));
        }
        String toolContext = toolOrchestrationService.buildToolContext(toolResults);
        if (StringUtils.hasText(toolContext)) {
            // 工具结果作为事实上下文追加给模型，避免把工具执行权交给模型自由发挥。
            messages.add(new ChatCompletionMessage("system", toolContext));
        }
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

    private void touchConversation(Long conversationId, LocalDateTime fallbackTime) {
        LocalDateTime time = fallbackTime == null ? LocalDateTime.now() : fallbackTime;
        AiConversationPO conversation = new AiConversationPO();
        conversation.setId(conversationId);
        conversation.setLastMessageTime(time);
        conversationMapper.updateById(conversation);
    }

    private ChatResultVO toChatResult(Long conversationId, AiMessagePO message, List<ToolCallResult> toolResults) {
        ChatResultVO vo = new ChatResultVO();
        vo.setConversationId(conversationId);
        vo.setMessageId(message.getId());
        vo.setContent(message.getContent());
        vo.setSources(Collections.<AiSourceVO>emptyList());
        vo.setToolCalls(toolResults == null
                ? Collections.<AiToolCallVO>emptyList()
                : toolResults.stream().map(ToolCallResult::toAiToolCallVO).toList());
        vo.setStatus(message.getStatus());
        vo.setModelName(message.getModelName());
        vo.setPromptTokens(message.getPromptTokens());
        vo.setCompletionTokens(message.getCompletionTokens());
        vo.setTotalTokens(message.getTotalTokens());
        vo.setFinishReason(message.getFinishReason());
        vo.setErrorMessage(message.getErrorMessage());
        return vo;
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

    private String buildTitle(String message) {
        String text = message == null ? "新的 AI 会话" : message.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(text)) {
            return "新的 AI 会话";
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
}
