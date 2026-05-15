package com.elysia.mooc.message.service;

import com.elysia.mooc.common.enums.MessageType;

/** 站内信写入服务，供业务事件消费者生成用户通知。 */
public interface MessageCommandService {

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
    Long sendToUser(Long senderId, Long receiverId, MessageType type, String title, String content, String linkUrl);
}
