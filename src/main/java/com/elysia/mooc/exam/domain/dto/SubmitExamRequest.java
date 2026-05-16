package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.exam.constants.ExamConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/** 提交试卷作答请求。 */
@Data
public class SubmitExamRequest implements Checker {

    /** 试卷 ID。 */
    @NotNull(message = "试卷ID不能为空")
    private Long paperId;

    /** 答案列表。 */
    @Valid
    private List<SubmitAnswerRequest> answers;

    @Override
    public void check() {
        if (paperId == null || paperId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "试卷ID必须为正数");
        }
        if (answers == null || answers.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "作答列表不能为空");
        }
        if (answers.size() > ExamConstants.MAX_SUBMIT_ANSWER_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "单次提交答案不能超过100条");
        }
        if (answers.stream().anyMatch(answer -> answer == null
                || answer.getQuestionId() == null
                || answer.getQuestionId() <= 0)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "题目ID必须为正数");
        }
        long distinctCount = answers.stream()
                .map(SubmitAnswerRequest::getQuestionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (distinctCount != answers.size()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "同一道题不能重复提交答案");
        }
    }
}
