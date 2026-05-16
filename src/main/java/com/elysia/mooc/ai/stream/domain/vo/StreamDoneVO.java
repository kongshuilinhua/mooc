package com.elysia.mooc.ai.stream.domain.vo;

import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import lombok.Data;

/** SSE done 事件数据。 */
@Data
public class StreamDoneVO {

    /** 会话 ID。 */
    private Long conversationId;

    /** 助手消息 ID。 */
    private Long messageId;

    /** 最终消息状态。 */
    private AiMessageStatus status;

    /** 模型结束原因。 */
    private String finishReason;

    /** 提示词 token 数。 */
    private Integer promptTokens;

    /** 回复 token 数。 */
    private Integer completionTokens;

    /** 总 token 数。 */
    private Integer totalTokens;
}
