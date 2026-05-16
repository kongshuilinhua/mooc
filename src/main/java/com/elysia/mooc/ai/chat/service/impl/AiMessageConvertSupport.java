package com.elysia.mooc.ai.chat.service.impl;

import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.chat.domain.vo.AiToolCallVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationDetailVO;
import com.elysia.mooc.ai.chat.domain.vo.ConversationVO;
import com.elysia.mooc.ai.chat.domain.vo.MessageVO;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import java.util.Collections;
import java.util.List;

/** AI 会话和消息 VO 转换支持。 */
final class AiMessageConvertSupport {

    private AiMessageConvertSupport() {
    }

    static ConversationVO toConversationVO(AiConversationPO po) {
        return BeanCopyUtils.copyBean(po, ConversationVO.class);
    }

    static ConversationDetailVO toConversationDetailVO(AiConversationPO po, List<MessageVO> messages) {
        ConversationDetailVO vo = BeanCopyUtils.copyBean(po, ConversationDetailVO.class);
        vo.setMessages(messages == null ? Collections.emptyList() : messages);
        return vo;
    }

    static MessageVO toMessageVO(AiMessagePO po) {
        return BeanCopyUtils.copyBean(po, MessageVO.class, (source, target) -> {
            target.setSources(Collections.<AiSourceVO>emptyList());
            target.setToolCalls(Collections.<AiToolCallVO>emptyList());
        });
    }
}
