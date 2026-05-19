package com.elysia.mooc.teaching.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 教师看板总览响应。 */
@Data
public class TeacherDashboardOverviewVO {

    /** 统计开始日期。 */
    private LocalDate startDate;

    /** 统计结束日期。 */
    private LocalDate endDate;

    /** 教师课程数。 */
    private Integer courseCount;

    /** 活跃学员数。 */
    private Integer activeStudentCount;

    /** 平均完课率。 */
    private BigDecimal averageCompletionRate;

    /** 收入金额汇总。 */
    private BigDecimal incomeAmount;

    /** 退款金额汇总。 */
    private BigDecimal refundAmount;

    /** 支付订单数汇总。 */
    private Integer paidOrderCount;

    /** 旧前端兼容字段：待回复问题数。 */
    private Integer pendingQuestionCount;

    /** 旧前端兼容字段：待审核课程数。 */
    private Integer pendingReviewCourseCount;

    /** 旧前端兼容字段：作业数量。 */
    private Integer homeworkCount;
}
