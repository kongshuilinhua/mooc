package com.elysia.mooc.course.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.course.domain.dto.AuditCourseRequest;
import com.elysia.mooc.course.domain.dto.CourseAuditLogQuery;
import com.elysia.mooc.course.domain.dto.OfflineCourseRequest;
import com.elysia.mooc.course.domain.dto.RejectCourseRequest;
import com.elysia.mooc.course.domain.dto.SubmitCourseAuditRequest;
import com.elysia.mooc.course.domain.vo.CourseAuditLogVO;

/** 课程审核发布服务。 */
public interface CourseAuditService {

    /**
     * 提交课程审核。
     *
     * @param courseId 课程 ID
     * @param request 提交审核请求，可为空
     * @return 操作成功返回 true
     */
    Boolean submitAudit(Long courseId, SubmitCourseAuditRequest request);

    /**
     * 审核通过课程。
     *
     * @param courseId 课程 ID
     * @param request 审核通过请求，可为空
     * @return 操作成功返回 true
     */
    Boolean approveCourse(Long courseId, AuditCourseRequest request);

    /**
     * 驳回课程审核。
     *
     * @param courseId 课程 ID
     * @param request 驳回请求
     * @return 操作成功返回 true
     */
    Boolean rejectCourse(Long courseId, RejectCourseRequest request);

    /**
     * 下架已发布课程。
     *
     * @param courseId 课程 ID
     * @param request 下架请求
     * @return 操作成功返回 true
     */
    Boolean offlineCourse(Long courseId, OfflineCourseRequest request);

    /**
     * 分页查询课程审核日志。
     *
     * @param courseId 课程 ID
     * @param query 查询参数
     * @return 审核日志分页
     */
    PageResult<CourseAuditLogVO> listAuditLogs(Long courseId, CourseAuditLogQuery query);
}
