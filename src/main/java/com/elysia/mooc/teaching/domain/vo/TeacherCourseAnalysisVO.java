package com.elysia.mooc.teaching.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 教师端单课程分析响应。 */
@Data
public class TeacherCourseAnalysisVO {

    /** 统计开始日期。 */
    private LocalDate startDate;

    /** 统计结束日期。 */
    private LocalDate endDate;

    /** 课程 ID。 */
    private Long courseId;

    /** 课程名称。 */
    private String courseName;

    /** 访问次数。 */
    private Integer viewCount;

    /** 学习人数。 */
    private Integer learnCount;

    /** 完课率。 */
    private BigDecimal completionRate;

    /** 支付订单数。 */
    private Integer paidOrderCount;

    /** 收入金额。 */
    private BigDecimal incomeAmount;

    /** 退款金额。 */
    private BigDecimal refundAmount;

    /** 旧前端兼容字段：平均学习分钟数。 */
    private Integer averageLearningMinutes;

    /** 旧前端兼容字段：互动数。 */
    private Integer interactionCount;

    /** 指标来源说明。 */
    private String note;
}
