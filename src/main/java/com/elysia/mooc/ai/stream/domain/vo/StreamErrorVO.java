package com.elysia.mooc.ai.stream.domain.vo;

import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import lombok.Data;

/** SSE error 事件数据。 */
@Data
public class StreamErrorVO {

    /** 会话 ID。 */
    private Long conversationId;

    /** 助手消息 ID。 */
    private Long messageId;

    /** 失败状态。 */
    private AiMessageStatus status;

    /** 中文错误提示。 */
    private String errorMessage;
}
