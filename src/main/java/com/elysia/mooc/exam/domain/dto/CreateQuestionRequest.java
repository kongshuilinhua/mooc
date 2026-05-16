package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建题目请求。 */
@Data
public class CreateQuestionRequest implements Checker {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 小节 ID。 */
    private Long sectionId;

    /** 题型。 */
    @NotNull(message = "题型不能为空")
    private ExamQuestionType questionType;

    /** 题干，兼容旧字段 content。 */
    @JsonAlias("content")
    @Size(max = 4000, message = "题干不能超过4000个字符")
    private String stem;

    /** 解析。 */
    @Size(max = 4000, message = "解析不能超过4000个字符")
    private String analysis;

    /** 参考答案，兼容旧字段 answer。 */
    @JsonAlias("answer")
    @Size(max = 4000, message = "参考答案不能超过4000个字符")
    private String answerText;

    /** 难度。 */
    private ExamDifficulty difficulty = ExamDifficulty.BEGINNER;

    /** 分值。 */
    @DecimalMin(value = "0.01", message = "题目分值必须大于0")
    private BigDecimal score = BigDecimal.ONE;

    /** 状态。 */
    private EnableStatus status = EnableStatus.ENABLED;

    /** 选项列表。 */
    @Valid
    private List<CreateQuestionOptionRequest> options;

    @Override
    public void check() {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程ID必须为正数");
        }
        if (sectionId != null && sectionId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "小节ID必须为正数");
        }
        if (!StringUtils.hasText(stem)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "题干不能为空");
        }
        if (questionType == ExamQuestionType.SHORT) {
            options = Collections.emptyList();
            return;
        }
        if (!StringUtils.hasText(answerText)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "客观题参考答案不能为空");
        }
        if (options == null || options.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "客观题选项不能为空");
        }
        long correctCount = options.stream().filter(option -> Boolean.TRUE.equals(option.getCorrect())).count();
        if (questionType == ExamQuestionType.SINGLE && correctCount != 1) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "单选题必须且只能有一个正确选项");
        }
        if (questionType == ExamQuestionType.MULTI && correctCount < 2) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "多选题至少需要两个正确选项");
        }
        if (questionType == ExamQuestionType.JUDGE && correctCount != 1) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "判断题必须且只能有一个正确选项");
        }
    }
}
