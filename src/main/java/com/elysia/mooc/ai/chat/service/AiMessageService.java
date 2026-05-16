package com.elysia.mooc.ai.chat.service;

import com.elysia.mooc.ai.chat.domain.vo.MessageVO;
import java.util.List;

/** AI 消息查询服务。 */
public interface AiMessageService {

    /**
     * 查询指定会话消息。
     *
     * @param conversationId 会话 ID
     * @return 消息列表
     */
    List<MessageVO> listMessages(Long conversationId);
}
