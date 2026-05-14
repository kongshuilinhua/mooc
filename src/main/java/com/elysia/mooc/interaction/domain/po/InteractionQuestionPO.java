package com.elysia.mooc.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程问答问题实体，映射 interaction_question 表。 */
@Data
@TableName("interaction_question")
public class InteractionQuestionPO {

    /** 问题 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 提问用户 ID。 */
    private Long userId;

    /** 问题标题。 */
    private String title;

    /** 问题内容。 */
    private String content;

    /** 回答数量。 */
    private Integer answerCount;

    /** 问题状态。 */
    private QuestionStatus status;

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
