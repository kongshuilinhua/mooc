package com.elysia.mooc.ai.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.chat.constants.AiChatErrorCode;
import com.elysia.mooc.ai.chat.domain.dto.AiConversationQuery;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationDetailVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationVO;
import com.elysia.mooc.ai.chat.domain.vo.MessageVO;
import com.elysia.mooc.ai.chat.mapper.AiConversationMapper;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.chat.service.AiConversationService;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** AI 会话管理服务实现。 */
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final UserContextService userContextService;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    /**
     * 分页查询当前用户会话。
     *
     * @param query 查询条件
     * @return 会话分页
     */
    @Override
    public PageResult<ConversationVO> listConversations(AiConversationQuery query) {
        Long userId = userContextService.currentUserId();
        AiConversationQuery safeQuery = query == null ? new AiConversationQuery() : query;

        Page<AiConversationPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        LambdaQueryWrapper<AiConversationPO> wrapper = new LambdaQueryWrapper<AiConversationPO>()
                .eq(AiConversationPO::getUserId, userId);
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(AiConversationPO::getTitle, safeQuery.getKeyword().trim());
        }
        applyOrder(wrapper, safeQuery);
        Page<AiConversationPO> result = conversationMapper.selectPage(page, wrapper);
        return PageResult.of(result, AiMessageConvertSupport::toConversationVO);
    }

    /**
     * 查询当前用户会话详情。
     *
     * @param id 会话 ID
     * @return 会话详情
     */
    @Override
    public ConversationDetailVO getConversation(Long id) {
        AiConversationPO conversation = requireOwnConversation(id);
        List<MessageVO> messages = messageMapper.selectList(new LambdaQueryWrapper<AiMessagePO>()
                        .eq(AiMessagePO::getConversationId, id)
                        .eq(AiMessagePO::getUserId, conversation.getUserId())
                        .orderByAsc(AiMessagePO::getCreateTime)
                        .orderByAsc(AiMessagePO::getId))
                .stream()
                .map(AiMessageConvertSupport::toMessageVO)
                .toList();
        return AiMessageConvertSupport.toConversationDetailVO(conversation, messages);
    }

    /**
     * 逻辑删除当前用户会话及其消息。
     *
     * @param id 会话 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteConversation(Long id) {
        AiConversationPO conversation = requireOwnConversation(id);
        conversationMapper.deleteById(conversation.getId());
        messageMapper.delete(new LambdaUpdateWrapper<AiMessagePO>()
                .eq(AiMessagePO::getConversationId, conversation.getId())
                .eq(AiMessagePO::getUserId, conversation.getUserId()));
        return true;
    }

    private AiConversationPO requireOwnConversation(Long id) {
        if (id == null || id < 1) {
            throw new BizException(AiChatErrorCode.AI_CHAT_PARAM_INVALID, "会话ID不正确");
        }
        Long userId = userContextService.currentUserId();
        AiConversationPO conversation = conversationMapper.selectById(id);
        if (conversation == null) {
            throw new BizException(AiChatErrorCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!userId.equals(conversation.getUserId())) {
            throw new BizException(AiChatErrorCode.AI_CHAT_FORBIDDEN);
        }
        return conversation;
    }

    private void applyOrder(LambdaQueryWrapper<AiConversationPO> wrapper, AiConversationQuery query) {
        boolean asc = Boolean.TRUE.equals(query.getIsAsc());
        String sortBy = query.getSortBy();
        if ("createTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiConversationPO::getCreateTime);
        } else if ("updateTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiConversationPO::getUpdateTime);
        } else if ("id".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiConversationPO::getId);
        } else {
            wrapper.orderBy(true, asc, AiConversationPO::getLastMessageTime);
        }
        wrapper.orderByDesc(AiConversationPO::getId);
    }
}
