package com.elysia.mooc.ai.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.MessageVO;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.chat.service.AiMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** AI 消息查询服务实现。 */
@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl implements AiMessageService {

    private final AiMessageMapper messageMapper;

    /**
     * 查询会话消息。
     *
     * @param conversationId 会话 ID
     * @return 消息列表
     */
    @Override
    public List<MessageVO> listMessages(Long conversationId) {
        return messageMapper.selectList(new LambdaQueryWrapper<AiMessagePO>()
                        .eq(AiMessagePO::getConversationId, conversationId)
                        .orderByAsc(AiMessagePO::getCreateTime)
                        .orderByAsc(AiMessagePO::getId))
                .stream()
                .map(AiMessageConvertSupport::toMessageVO)
                .toList();
    }
}
