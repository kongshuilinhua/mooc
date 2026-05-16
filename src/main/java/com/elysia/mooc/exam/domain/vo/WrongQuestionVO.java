package com.elysia.mooc.exam.domain.vo;

import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.time.LocalDateTime;
import lombok.Data;

/** 错题本响应。 */
@Data
public class WrongQuestionVO {

    /** 错题记录 ID。 */
    private Long id;

    /** 题目 ID。 */
    private Long questionId;

    /** 课程 ID。 */
    private Long courseId;

    /** 题型。 */
    private ExamQuestionType questionType;

    /** 题干。 */
    private String stem;

    /** 参考答案。 */
    private String answerText;

    /** 解析。 */
    private String analysis;

    /** 错误次数。 */
    private Integer wrongCount;

    /** 是否已解决。 */
    private Boolean resolved;

    /** 最近答错时间。 */
    private LocalDateTime lastWrongTime;
}
