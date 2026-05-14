package com.elysia.mooc.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.interaction.domain.enums.AnswerAcceptedStatus;
import com.elysia.mooc.interaction.domain.enums.AnswerStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程问答回答实体，映射 interaction_answer 表。 */
@Data
@TableName("interaction_answer")
public class InteractionAnswerPO {

    /** 回答 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 问题 ID。 */
    private Long questionId;

    /** 回答用户 ID。 */
    private Long userId;

    /** 回答内容。 */
    private String content;

    /** 是否被采纳。 */
    private AnswerAcceptedStatus accepted;

    /** 点赞数量。 */
    private Integer likeCount;

    /** 回答状态。 */
    private AnswerStatus status;

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
