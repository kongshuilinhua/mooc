package com.elysia.mooc.message.service.impl;

import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.common.enums.ReadStatus;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.message.domain.po.MessagePO;
import com.elysia.mooc.message.domain.po.MessageReceiverPO;
import com.elysia.mooc.message.mapper.MessageMapper;
import com.elysia.mooc.message.mapper.MessageReceiverMapper;
import com.elysia.mooc.message.service.MessageCommandService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 站内信写入服务默认实现。 */
@Service
@RequiredArgsConstructor
public class MessageCommandServiceImpl implements MessageCommandService {

    private final MessageMapper messageMapper;
    private final MessageReceiverMapper messageReceiverMapper;

    /**
     * 给单个用户发送站内信。
     *
     * @param senderId 发送人 ID，系统通知可为空
     * @param receiverId 接收人 ID
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息正文
     * @param linkUrl 跳转地址，可为空
     * @return 消息 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendToUser(Long senderId, Long receiverId, MessageType type, String title, String content, String linkUrl) {
        if (receiverId == null || receiverId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "消息接收人不能为空");
        }
        if (type == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "消息类型不能为空");
        }
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "消息标题和内容不能为空");
        }

        MessagePO message = new MessagePO();
        message.setSenderId(senderId);
        message.setType(type);
        message.setTitle(title.trim());
        message.setContent(content.trim());
        message.setLinkUrl(StringUtils.hasText(linkUrl) ? linkUrl.trim() : null);
        message.setDeleted(0);
        messageMapper.insert(message);

        MessageReceiverPO receiver = new MessageReceiverPO();
        receiver.setMessageId(message.getId());
        receiver.setReceiverId(receiverId);
        receiver.setReadStatus(ReadStatus.UNREAD);
        receiver.setDeleted(0);
        receiver.setCreateTime(LocalDateTime.now());
        messageReceiverMapper.insert(receiver);
        return message.getId();
    }
}
