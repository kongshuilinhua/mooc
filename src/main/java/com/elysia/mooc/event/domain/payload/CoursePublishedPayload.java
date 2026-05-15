package com.elysia.mooc.event.domain.payload;

import java.time.LocalDateTime;

/** 课程发布事件载荷。 */
public record CoursePublishedPayload(
        Long courseId,
        String courseTitle,
        Long teacherId,
        LocalDateTime publishTime) {
}
