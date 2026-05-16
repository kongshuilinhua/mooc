package com.elysia.mooc.ai.tool.domain.vo;

import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import lombok.Data;

/** Tool 调用日志展示对象。 */
@Data
public class ToolCallLogVO {

    /** 主键 ID。 */
    private Long id;

    /** AI 会话 ID。 */
    private Long conversationId;

    /** 触发工具调用的消息 ID。 */
    private Long messageId;

    /** 用户 ID。 */
    private Long userId;

    /** 工具名称。 */
    private String toolName;

    /** 工具入参。 */
    private Map<String, Object> arguments = Collections.emptyMap();

    /** 兼容前端日志展示的工具入参。 */
    private Map<String, Object> argumentsJson = Collections.emptyMap();

    /** 工具结果。 */
    private Map<String, Object> result = Collections.emptyMap();

    /** 兼容前端日志展示的工具结果。 */
    private Map<String, Object> resultJson = Collections.emptyMap();

    /** 调用状态。 */
    private ToolCallStatus status;

    /** 耗时毫秒。 */
    private Integer costMs;

    /** 中文失败原因。 */
    private String errorMessage;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
