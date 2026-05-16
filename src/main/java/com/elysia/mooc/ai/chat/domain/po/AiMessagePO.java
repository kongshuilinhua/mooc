package com.elysia.mooc.ai.chat.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageRole;
import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 消息实体，映射 ai_message 表。 */
@Data
@TableName("ai_message")
public class AiMessagePO {

    /** 消息 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属会话 ID。 */
    private Long conversationId;

    /** 消息所属用户 ID。 */
    private Long userId;

    /** 消息角色。 */
    private AiMessageRole role;

    /** 消息正文。 */
    private String content;

    /** 消息处理状态。 */
    private AiMessageStatus status;

    /** 生成该消息使用的模型名称。 */
    private String modelName;

    /** 提示词 token 数。 */
    private Integer promptTokens;

    /** 回复 token 数。 */
    private Integer completionTokens;

    /** 总 token 数。 */
    private Integer totalTokens;

    /** 模型结束原因。 */
    private String finishReason;

    /** 引用信息 JSON，day15 普通聊天默认空数组。 */
    private String citations;

    /** 用户反馈，day35 复用。 */
    private String feedback;

    /** 错误信息。 */
    private String errorMessage;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
