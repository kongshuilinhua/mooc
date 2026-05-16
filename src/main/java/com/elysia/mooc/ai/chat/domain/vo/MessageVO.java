package com.elysia.mooc.ai.chat.domain.vo;

import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** AI 消息响应对象。 */
@Data
public class MessageVO {

    /** 消息 ID。 */
    private Long id;

    /** 所属会话 ID。 */
    private Long conversationId;

    /** 消息所属用户 ID。 */
    private Long userId;

    /** 消息角色。 */
    private AiMessageRole role;

    /** 消息正文。 */
    private String content;

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

    /** 模型结束原因。 */
    private String finishReason;

    /** 错误信息。 */
    private String errorMessage;

    /** 引用来源。 */
    private List<AiSourceVO> sources;

    /** 工具调用。 */
    private List<AiToolCallVO> toolCalls;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
