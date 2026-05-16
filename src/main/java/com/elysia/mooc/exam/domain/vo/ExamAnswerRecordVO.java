package com.elysia.mooc.exam.domain.vo;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/** 单题作答结果。 */
@Data
@Builder
public class ExamAnswerRecordVO {

    /** 题目 ID。 */
    private Long questionId;

    /** 用户答案。 */
    private String answerContent;

    /** 是否正确，简答题为 null。 */
    private Boolean correct;

    /** 本题得分。 */
    private BigDecimal score;

    /** 判分说明。 */
    private String teacherComment;
}
