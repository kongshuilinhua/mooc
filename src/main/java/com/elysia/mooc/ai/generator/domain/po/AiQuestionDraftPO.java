package com.elysia.mooc.ai.generator.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 题目草稿实体，映射 ai_question_draft 表。 */
@Data
@TableName("ai_question_draft")
public class AiQuestionDraftPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 生成任务 ID。 */
    private Long taskId;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节 ID。 */
    private Long chapterId;

    /** 题型。 */
    private ExamQuestionType questionType;

    /** 题目内容快照，SQL 字段为 LONGTEXT。 */
    private String questionContent;

    /** 难度。 */
    private ExamDifficulty difficultyLevel;

    /** 审核状态。 */
    private AiQuestionReviewStatus reviewStatus;

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
