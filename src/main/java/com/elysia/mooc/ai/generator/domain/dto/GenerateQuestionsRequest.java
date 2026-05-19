package com.elysia.mooc.ai.generator.domain.dto;

import com.elysia.mooc.ai.generator.constants.AiGeneratorConstants;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import lombok.Data;

/** 生成练习题草稿请求。 */
@Data
public class GenerateQuestionsRequest implements Checker {

    /** 题目数量。 */
    private Integer questionCount = AiGeneratorConstants.DEFAULT_QUESTION_COUNT;

    /** 题目难度。 */
    private ExamDifficulty difficulty = ExamDifficulty.MEDIUM;

    /** 题型。 */
    private ExamQuestionType questionType = ExamQuestionType.SINGLE;

    /** 可选章节 ID。 */
    private Long chapterId;

    @Override
    public void check() {
        if (questionCount == null) {
            questionCount = AiGeneratorConstants.DEFAULT_QUESTION_COUNT;
        }
        if (questionCount < 1 || questionCount > AiGeneratorConstants.MAX_QUESTION_COUNT) {
            throw new BizException(CommonErrorCode.PARAM_INVALID,
                    "题目数量必须在1到" + AiGeneratorConstants.MAX_QUESTION_COUNT + "之间");
        }
        if (difficulty == null) {
            difficulty = ExamDifficulty.MEDIUM;
        }
        if (questionType == null) {
            questionType = ExamQuestionType.SINGLE;
        }
        if (chapterId != null && chapterId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "章节ID必须为正数");
        }
    }
}
