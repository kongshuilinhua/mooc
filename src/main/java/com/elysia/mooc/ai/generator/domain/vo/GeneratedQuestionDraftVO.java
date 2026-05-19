package com.elysia.mooc.ai.generator.domain.vo;

import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.util.List;
import lombok.Data;

/** AI 生成题目草稿。 */
@Data
public class GeneratedQuestionDraftVO {

    /** 草稿 ID。 */
    private Long draftId;

    /** 题型。 */
    private ExamQuestionType type;

    /** 题干。 */
    private String stem;

    /** 选项文本。 */
    private List<String> options;

    /** 参考答案。 */
    private String answer;

    /** 解析。 */
    private String analysis;

    /** 难度。 */
    private ExamDifficulty difficulty;

    /** 审核状态。 */
    private AiQuestionReviewStatus reviewStatus;
}
