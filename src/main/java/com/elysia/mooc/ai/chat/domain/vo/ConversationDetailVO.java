package com.elysia.mooc.ai.chat.domain.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** AI 会话详情响应对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConversationDetailVO extends ConversationVO {

    /** 会话消息列表。 */
    private List<MessageVO> messages;
}
