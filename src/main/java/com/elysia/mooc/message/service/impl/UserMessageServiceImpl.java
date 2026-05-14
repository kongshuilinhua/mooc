package com.elysia.mooc.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.message.domain.dto.MarkMessagesReadRequest;
import com.elysia.mooc.message.domain.dto.UserMessageQuery;
import com.elysia.mooc.message.domain.po.MessageReceiverPO;
import com.elysia.mooc.message.domain.vo.MessageVO;
import com.elysia.mooc.message.mapper.MessageMapper;
import com.elysia.mooc.message.mapper.MessageReceiverMapper;
import com.elysia.mooc.message.service.UserMessageService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用户消息服务默认实现。 */
@Service
@RequiredArgsConstructor
public class UserMessageServiceImpl implements UserMessageService {

    private final UserContextService userContextService;
    private final MessageMapper messageMapper;
    private final MessageReceiverMapper messageReceiverMapper;

    /**
     * 查询当前用户未读消息数。
     * @return 未读数量
     */
    @Override
    public Map<String, Integer> getUnreadCount() {
        Long userId = userContextService.currentUserId();
        LambdaQueryWrapper<MessageReceiverPO> wrapper = Wrappers.<MessageReceiverPO>lambdaQuery()
                .eq(MessageReceiverPO::getReceiverId, userId)
                .eq(MessageReceiverPO::getReadStatus, ReadStatus.UNREAD)
                .inSql(MessageReceiverPO::getMessageId, activeMessageSql());
        int unreadCount = toInt(messageReceiverMapper.selectCount(wrapper));
        return Map.of("unreadCount", unreadCount);
    }

    /**
     * 分页查询当前用户消息。
     * @param query 分页筛选参数
     * @return 分页结果
     */
    @Override
    public PageResult<MessageVO> listMessages(UserMessageQuery query) {
        UserMessageQuery safeQuery = query == null ? new UserMessageQuery() : query;
        Long userId = userContextService.currentUserId();
        Page<MessageVO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<MessageVO> result = messageMapper.selectUserMessagePage(
                page,
                userId,
                safeQuery.getType(),
                safeQuery.getIsRead());
        return PageResult.of(result);
    }

    /**
     * 批量标记已读。
     * @param request 消息ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markRead(MarkMessagesReadRequest request) {
        Long userId = userContextService.currentUserId();

        // 只按当前用户和未读状态更新，避免批量接口越权修改其他接收人的消息状态。
        MessageReceiverPO update = new MessageReceiverPO();
        update.setReadStatus(ReadStatus.READ);
        update.setReadTime(LocalDateTime.now());
        messageReceiverMapper.update(update, Wrappers.<MessageReceiverPO>lambdaUpdate()
                .eq(MessageReceiverPO::getReceiverId, userId)
                .eq(MessageReceiverPO::getReadStatus, ReadStatus.UNREAD)
                .in(MessageReceiverPO::getMessageId, request.getMessageIds())
                .inSql(MessageReceiverPO::getMessageId, activeMessageSql()));
        return true;
    }

    private String activeMessageSql() {
        return "SELECT id FROM message WHERE deleted = 0";
    }

    private int toInt(Long value) {
        if (value == null || value <= 0) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }
}
