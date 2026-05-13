package com.elysia.mooc.course.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.course.domain.dto.CoursePageQuery;
import com.elysia.mooc.course.domain.dto.CreateCourseRequest;
import com.elysia.mooc.course.domain.dto.UpdateCourseRequest;
import com.elysia.mooc.course.domain.vo.CourseCardVO;
import com.elysia.mooc.course.domain.vo.CourseDetailVO;
import com.elysia.mooc.course.domain.vo.CourseMutationVO;

/** 课程基础信息服务。 */
public interface CourseService {

    /**
     * 分页查询课程。
     *
     * @param query 查询条件
     * @return 课程分页
     */
    PageResult<CourseCardVO> listCourses(CoursePageQuery query);

    /**
     * 查询课程详情。
     *
     * @param courseId 课程 ID
     * @return 课程详情
     */
    CourseDetailVO getCourseDetail(Long courseId);

    /**
     * 创建课程。
     *
     * @param request 创建课程请求
     * @return 课程变更结果
     */
    CourseMutationVO createCourse(CreateCourseRequest request);

    /**
     * 修改课程。
     *
     * @param courseId 课程 ID
     * @param request  修改课程请求
     * @return 课程变更结果
     */
    CourseMutationVO updateCourse(Long courseId, UpdateCourseRequest request);
}
