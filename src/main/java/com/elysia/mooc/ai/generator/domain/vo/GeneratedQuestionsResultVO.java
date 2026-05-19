package com.elysia.mooc.ai.generator.domain.vo;

import com.elysia.mooc.ai.generator.domain.enums.AiGenerationStatus;
import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import java.util.List;
import lombok.Data;

/** AI 练习题草稿生成结果。 */
@Data
public class GeneratedQuestionsResultVO {

    /** 生成任务 ID。 */
    private Long taskId;

    /** 课程 ID。 */
    private Long courseId;

    /** 题目数量。 */
    private Integer questionCount;

    /** 批次审核状态。 */
    private AiQuestionReviewStatus reviewStatus;

    /** 题目草稿列表。 */
    private List<GeneratedQuestionDraftVO> questions;

    /** 任务状态。 */
    private AiGenerationStatus status;

    /** 生成来源，模型或规则兜底。 */
    private String generationSource;

    /** 错误信息。 */
    private String errorMessage;
}
