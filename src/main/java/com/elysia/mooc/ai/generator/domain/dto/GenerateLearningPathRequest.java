package com.elysia.mooc.ai.generator.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.elysia.mooc.ai.generator.constants.AiGeneratorConstants;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 生成学习路径请求。 */
@Data
public class GenerateLearningPathRequest implements Checker {

    /** 学习目标，兼容前端 target 字段。 */
    @JsonAlias("goal")
    @Size(max = 200, message = "学习目标不能超过200个字符")
    private String target;

    /** 目标类型。 */
    private String goalType = "COURSE_MASTER";

    /** 目标课程 ID。 */
    private Long targetCourseId;

    /** 规划周期天数。 */
    private Integer horizonDays = AiGeneratorConstants.DEFAULT_HORIZON_DAYS;

    /** 每日建议学习分钟数。 */
    private Integer dailyMinutes = AiGeneratorConstants.DEFAULT_DAILY_MINUTES;

    @Override
    public void check() {
        if (!StringUtils.hasText(target)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "学习目标不能为空");
        }
        if (!StringUtils.hasText(goalType)) {
            goalType = "COURSE_MASTER";
        }
        if (targetCourseId != null && targetCourseId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "目标课程ID必须为正数");
        }
        if (horizonDays == null) {
            horizonDays = AiGeneratorConstants.DEFAULT_HORIZON_DAYS;
        }
        if (horizonDays < 7 || horizonDays > AiGeneratorConstants.MAX_HORIZON_DAYS) {
            throw new BizException(CommonErrorCode.PARAM_INVALID,
                    "学习周期必须在7到" + AiGeneratorConstants.MAX_HORIZON_DAYS + "天之间");
        }
        if (dailyMinutes == null) {
            dailyMinutes = AiGeneratorConstants.DEFAULT_DAILY_MINUTES;
        }
        if (dailyMinutes < 10 || dailyMinutes > 240) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "每日学习时长必须在10到240分钟之间");
        }
    }
}
