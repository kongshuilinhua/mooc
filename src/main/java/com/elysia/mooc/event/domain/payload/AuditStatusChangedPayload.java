package com.elysia.mooc.event.domain.payload;

import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;

/** 课程审核状态变更事件载荷。 */
public record AuditStatusChangedPayload(
        Long courseId,
        String courseTitle,
        Long teacherId,
        CourseStatus beforeStatus,
        CourseStatus afterStatus,
        CourseAuditAction action,
        Long operatorId,
        String comment) {
}
