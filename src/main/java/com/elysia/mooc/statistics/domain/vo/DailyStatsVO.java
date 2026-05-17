package com.elysia.mooc.statistics.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.Data;

/** 后台每日统计视图对象。 */
@Data
public class DailyStatsVO {

    /** 统计日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statDate;

    /** 新增用户数。 */
    private Integer newUserCount;

    /** 活跃用户数。 */
    private Integer activeUserCount;

    /** 课程浏览次数。 */
    private Integer courseViewCount;

    /** 视频播放次数。 */
    private Integer videoPlayCount;

    /** 学习秒数。 */
    private Long learnSeconds;

    /** 学习分钟数。 */
    private Long learningMinutes;

    /** AI 调用次数。 */
    private Integer aiCallCount;
}
