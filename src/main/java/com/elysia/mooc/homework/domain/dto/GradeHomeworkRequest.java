package com.elysia.mooc.homework.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/** 教师批改作业请求。 */
@Data
public class GradeHomeworkRequest implements Checker {

    /** 分数。 */
    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.00", message = "分数不能小于0")
    @DecimalMax(value = "100.00", message = "分数不能大于100")
    private BigDecimal score;

    /** 批改评语，兼容 comment 字段。 */
    @JsonAlias("comment")
    @Size(max = 2000, message = "批改评语不能超过2000个字符")
    private String feedback;

    @Override
    public void check() {
        if (score == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "分数不能为空");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "分数必须在0到100之间");
        }
        if (feedback != null) {
            feedback = feedback.trim();
        }
    }
}
