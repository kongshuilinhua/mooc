package com.elysia.mooc.statistics.domain.vo;

import lombok.Data;

/** 后台数据概览视图对象。 */
@Data
public class AdminOverviewVO {

    /** 用户总数。 */
    private Long userCount;

    /** 新增用户数。 */
    private Integer newUserCount;

    /** 活跃用户数。 */
    private Integer activeUserCount;

    /** 课程总数。 */
    private Long courseCount;

    /** 课程浏览次数。 */
    private Integer courseViewCount;

    /** 视频播放次数。 */
    private Integer videoPlayCount;

    /** 学习分钟数。 */
    private Long learningMinutes;

    /** 学习秒数。 */
    private Long learnSeconds;

    /** AI 请求次数。 */
    private Integer aiRequestCount;
}
