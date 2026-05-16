package com.elysia.mooc.exam.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 题目响应。 */
@Data
public class QuestionVO {

    /** 题目 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 创建人 ID。 */
    private Long creatorId;

    /** 题型。 */
    private ExamQuestionType questionType;

    /** 题干。 */
    private String stem;

    /** 解析。 */
    private String analysis;

    /** 参考答案。 */
    private String answerText;

    /** 难度。 */
    private ExamDifficulty difficulty;

    /** 分值。 */
    private BigDecimal score;

    /** 状态。 */
    private EnableStatus status;

    /** 选项列表。 */
    private List<QuestionOptionVO> options;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
