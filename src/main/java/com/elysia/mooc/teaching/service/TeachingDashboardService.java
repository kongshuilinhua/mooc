package com.elysia.mooc.teaching.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.teaching.domain.dto.TeacherDashboardQuery;
import com.elysia.mooc.teaching.domain.dto.TeacherStudentProgressQuery;
import com.elysia.mooc.teaching.domain.vo.TeacherCourseAnalysisVO;
import com.elysia.mooc.teaching.domain.vo.TeacherDashboardOverviewVO;
import com.elysia.mooc.teaching.domain.vo.TeacherStudentProgressVO;

/** 教师数据看板服务。 */
public interface TeachingDashboardService {

    /**
     * 查询当前教师看板总览。
     * @param query 日期范围查询条件；未传时默认最近7天
     * @return 教师看板总览
     */
    TeacherDashboardOverviewVO getOverview(TeacherDashboardQuery query);

    /**
     * 查询当前教师名下单门课程分析。
     * @param courseId 课程 ID
     * @param query 日期范围查询条件；未传时默认最近7天
     * @return 课程分析数据
     */
    TeacherCourseAnalysisVO getCourseAnalysis(Long courseId, TeacherDashboardQuery query);

    /**
     * 分页查询当前教师名下课程的学员进度。
     * @param courseId 课程 ID
     * @param query 分页和风险筛选条件
     * @return 学员进度分页结果
     */
    PageResult<TeacherStudentProgressVO> listCourseStudents(Long courseId, TeacherStudentProgressQuery query);
}
