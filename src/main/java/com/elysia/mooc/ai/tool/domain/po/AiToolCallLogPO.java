package com.elysia.mooc.ai.tool.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.tool.domain.enums.ToolCallStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** Tool 调用日志实体，映射 ai_tool_call_log 表。 */
@Data
@TableName("ai_tool_call_log")
public class AiToolCallLogPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** AI 会话 ID。 */
    private Long conversationId;

    /** 触发工具调用的消息 ID。 */
    private Long messageId;

    /** 当前登录用户 ID。 */
    private Long userId;

    /** 工具名称。 */
    private String toolName;

    /** 工具入参 JSON。 */
    private String argumentsJson;

    /** 工具结果 JSON。 */
    private String resultJson;

    /** 调用状态。 */
    private ToolCallStatus status;

    /** 耗时毫秒。 */
    private Integer costMs;

    /** 中文失败原因。 */
    private String errorMessage;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
