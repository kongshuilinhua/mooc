package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/** 试卷题目关系实体，映射 exam_paper_question 表。 */
@Data
@TableName("exam_paper_question")
public class ExamPaperQuestionPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷 ID。 */
    private Long paperId;

    /** 题目 ID。 */
    private Long questionId;

    /** 本试卷中该题分值。 */
    private BigDecimal score;

    /** 排序值。 */
    private Integer sort;
}
