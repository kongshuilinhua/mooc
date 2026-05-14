package com.elysia.mooc.learning.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.learning.domain.dto.JoinCourseRequest;
import com.elysia.mooc.learning.domain.dto.LearningCourseQuery;
import com.elysia.mooc.learning.domain.dto.LearningHeartbeatRequest;
import com.elysia.mooc.learning.domain.dto.LearningHistoryQuery;
import com.elysia.mooc.learning.domain.vo.LearningCourseItem;
import com.elysia.mooc.learning.domain.vo.LearningHistoryItem;
import com.elysia.mooc.learning.domain.vo.LearningRecordVO;
import com.elysia.mooc.learning.domain.vo.LearningStatisticsVO;

/** 学习进度服务。 */
public interface LearningService {

    /**
     * 加入课程。
     *
     * @param request 加入课程请求
     * @return 是否加入成功
     */
    Boolean joinCourse(JoinCourseRequest request);

    /**
     * 分页查询我的课程。
     *
     * @param query 查询参数
     * @return 我的课程分页
     */
    PageResult<LearningCourseItem> listMyCourses(LearningCourseQuery query);

    /**
     * 上报学习心跳。
     *
     * @param request 心跳请求
     * @return 更新后的学习记录
     */
    LearningRecordVO heartbeat(LearningHeartbeatRequest request);

    /**
     * 分页查询学习历史。
     *
     * @param query 查询参数
     * @return 学习历史分页
     */
    PageResult<LearningHistoryItem> listHistory(LearningHistoryQuery query);

    /**
     * 查询当前用户学习统计。
     *
     * @return 学习统计
     */
    LearningStatisticsVO getStatistics();
}
