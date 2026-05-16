package com.elysia.mooc.ai.chat.domain.vo;

import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import java.util.List;
import lombok.Data;

/** 普通聊天响应。 */
@Data
public class ChatResultVO {

    /** 会话 ID。 */
    private Long conversationId;

    /** AI 回复消息 ID。 */
    private Long messageId;

    /** AI 回复正文。 */
    private String content;

    /** 引用来源，day15 固定为空列表。 */
    private List<AiSourceVO> sources;

    /** 工具调用，day15 固定为空列表。 */
    private List<AiToolCallVO> toolCalls;

    /** 消息状态。 */
    private AiMessageStatus status;

    /** 模型名称。 */
    private String modelName;

    /** 提示词 token 数。 */
    private Integer promptTokens;

    /** 回复 token 数。 */
    private Integer completionTokens;

    /** 总 token 数。 */
    private Integer totalTokens;

    /** 结束原因。 */
    private String finishReason;

    /** 错误信息。 */
    private String errorMessage;
}
