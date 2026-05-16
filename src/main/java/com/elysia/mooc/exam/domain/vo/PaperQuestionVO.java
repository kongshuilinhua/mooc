package com.elysia.mooc.exam.domain.vo;

import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** 试卷题目响应。 */
@Data
@Builder
public class PaperQuestionVO {

    /** 题目 ID。 */
    private Long questionId;

    /** 题型。 */
    private ExamQuestionType questionType;

    /** 题干。 */
    private String stem;

    /** 分值。 */
    private BigDecimal score;

    /** 排序值。 */
    private Integer sort;
}
