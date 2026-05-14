package com.elysia.mooc.learning.domain.vo;

import lombok.Builder;
import lombok.Data;

/** 学习统计视图。 */
@Data
@Builder
public class LearningStatisticsVO {

    /** 今日学习分钟数。 */
    private Integer todayMinutes;

    /** 累计学习分钟数。 */
    private Integer totalMinutes;

    /** 已完成课程数。 */
    private Integer completedCourseCount;

    /** 已完成小节数。 */
    private Integer completedSectionCount;
}
