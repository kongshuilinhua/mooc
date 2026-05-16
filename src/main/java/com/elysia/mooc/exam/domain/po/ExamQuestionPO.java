package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 题目实体，映射 exam_question 表。 */
@Data
@TableName("exam_question")
public class ExamQuestionPO {

    /** 题目 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 创建人 ID。 */
    private Long creatorId;

    /** 题目类型。 */
    private ExamQuestionType questionType;

    /** 题干。 */
    private String stem;

    /** 解析。 */
    private String analysis;

    /** 参考答案。 */
    private String answerText;

    /** 难度。 */
    private ExamDifficulty difficulty;

    /** 题目分值。 */
    private BigDecimal score;

    /** 启停状态。 */
    private EnableStatus status;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
