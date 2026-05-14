package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程审核日志视图对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAuditLogVO {

    /** 审核记录 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 变更前课程状态。 */
    private CourseStatus beforeStatus;

    /** 变更后课程状态。 */
    private CourseStatus afterStatus;

    /** 审核人或提交人用户 ID。 */
    private Long auditorId;

    /** 审核意见、驳回原因或下架原因。 */
    private String auditComment;

    /** 审核动作。 */
    private CourseAuditAction auditAction;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
