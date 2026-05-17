package com.elysia.mooc.course.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.audit.AuditLog;
import com.elysia.mooc.common.idempotent.Idempotent;
import com.elysia.mooc.course.domain.dto.AuditCourseRequest;
import com.elysia.mooc.course.domain.dto.CourseAuditLogQuery;
import com.elysia.mooc.course.domain.dto.OfflineCourseRequest;
import com.elysia.mooc.course.domain.dto.RejectCourseRequest;
import com.elysia.mooc.course.domain.dto.SubmitCourseAuditRequest;
import com.elysia.mooc.course.domain.vo.CourseAuditLogVO;
import com.elysia.mooc.course.service.CourseAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 课程审核与发布接口。 */
@Tag(name = "课程审核与发布")
@Validated
@RestController
@RequiredArgsConstructor
public class CourseAuditController {

    private final CourseAuditService courseAuditService;

    /**
     * 提交课程审核。
     *
     * @param courseId 课程 ID
     * @param request 提交审核请求，可为空
     * @return 是否提交成功
     */
    @Operation(summary = "提交课程审核")
    @PostMapping("/api/courses/{courseId}/submit-audit")
    @PreAuthorize("isAuthenticated()")
    @Idempotent(bizType = "COURSE_SUBMIT_AUDIT", bizId = "#courseId")
    public ApiResult<Boolean> submitAudit(
            @PathVariable Long courseId,
            @Valid @RequestBody(required = false) SubmitCourseAuditRequest request) {
        return ApiResult.ok(courseAuditService.submitAudit(courseId, request));
    }

    /**
     * 审核通过课程。
     *
     * @param courseId 课程 ID
     * @param request 审核通过请求，可为空
     * @return 是否审核成功
     */
    @Operation(summary = "审核通过课程")
    @PostMapping("/api/admin/courses/{courseId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "COURSE_APPROVE", targetType = "COURSE", targetId = "#courseId")
    @Idempotent(bizType = "COURSE_APPROVE", bizId = "#courseId")
    public ApiResult<Boolean> approveCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody(required = false) AuditCourseRequest request) {
        return ApiResult.ok(courseAuditService.approveCourse(courseId, request));
    }

    /**
     * 审核驳回课程。
     *
     * @param courseId 课程 ID
     * @param request 驳回请求
     * @return 是否驳回成功
     */
    @Operation(summary = "审核驳回课程")
    @PostMapping("/api/admin/courses/{courseId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "COURSE_REJECT", targetType = "COURSE", targetId = "#courseId")
    @Idempotent(bizType = "COURSE_REJECT", bizId = "#courseId")
    public ApiResult<Boolean> rejectCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody RejectCourseRequest request) {
        return ApiResult.ok(courseAuditService.rejectCourse(courseId, request));
    }

    /**
     * 下架已发布课程。
     *
     * @param courseId 课程 ID
     * @param request 下架请求
     * @return 是否下架成功
     */
    @Operation(summary = "下架已发布课程")
    @PostMapping("/api/admin/courses/{courseId}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "COURSE_OFFLINE", targetType = "COURSE", targetId = "#courseId")
    @Idempotent(bizType = "COURSE_OFFLINE", bizId = "#courseId")
    public ApiResult<Boolean> offlineCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody OfflineCourseRequest request) {
        return ApiResult.ok(courseAuditService.offlineCourse(courseId, request));
    }

    /**
     * 分页查询课程审核日志。
     *
     * @param courseId 课程 ID
     * @param query 查询条件
     * @return 审核日志分页
     */
    @Operation(summary = "分页查询课程审核日志")
    @GetMapping("/api/courses/{courseId}/audit-logs")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<CourseAuditLogVO>> listAuditLogs(
            @PathVariable Long courseId,
            @Valid CourseAuditLogQuery query) {
        return ApiResult.ok(courseAuditService.listAuditLogs(courseId, query));
    }
}
