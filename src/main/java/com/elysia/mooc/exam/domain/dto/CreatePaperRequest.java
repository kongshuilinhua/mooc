package com.elysia.mooc.exam.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.exam.constants.ExamConstants;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建试卷请求。 */
@Data
public class CreatePaperRequest implements Checker {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 试卷标题，兼容旧字段 paperName。 */
    @JsonAlias("paperName")
    @Size(max = 128, message = "试卷标题不能超过128个字符")
    private String title;

    /** 试卷描述。 */
    @Size(max = 500, message = "试卷描述不能超过500个字符")
    private String description;

    /** 及格分。 */
    @DecimalMin(value = "0", message = "及格分不能小于0")
    private BigDecimal passScore;

    /** 考试时长，单位分钟。 */
    private Integer durationMinutes;

    /** 试卷状态。 */
    private ExamPaperStatus status = ExamPaperStatus.PUBLISHED;

    /** 题目 ID 列表。 */
    private List<Long> questionIds;

    @Override
    public void check() {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程ID必须为正数");
        }
        if (!StringUtils.hasText(title)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "试卷标题不能为空");
        }
        if (durationMinutes != null && durationMinutes <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "考试时长必须为正数");
        }
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "试卷题目不能为空");
        }
        questionIds = questionIds.stream().filter(Objects::nonNull).distinct().toList();
        if (questionIds.isEmpty()) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "试卷题目不能为空");
        }
        if (questionIds.size() > ExamConstants.MAX_PAPER_QUESTION_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "单张试卷题目不能超过100道");
        }
        if (questionIds.stream().anyMatch(questionId -> questionId <= 0)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "题目ID必须为正数");
        }
    }
}
