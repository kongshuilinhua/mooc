package com.elysia.mooc.ai.chat.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.chat.domain.enums.AiConversationScene;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 会话实体，映射 ai_conversation 表。 */
@Data
@TableName("ai_conversation")
public class AiConversationPO {

    /** 会话 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话所属用户 ID。 */
    private Long userId;

    /** 会话标题。 */
    private String title;

    /** 会话场景。 */
    private AiConversationScene scene;

    /** 绑定知识库 ID，day15 普通聊天默认为空。 */
    private Long kbId;

    /** 绑定课程 ID。 */
    private Long courseId;

    /** 长会话摘要，day38 记忆能力复用。 */
    private String summary;

    /** 记忆策略，day15 固定使用 RECENT_N。 */
    private String memoryStrategy;

    /** 最后一条消息时间。 */
    private LocalDateTime lastMessageTime;

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
