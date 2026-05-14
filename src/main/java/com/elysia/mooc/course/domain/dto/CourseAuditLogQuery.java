package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 课程审核日志分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseAuditLogQuery extends PageQuery {

    /** 审核后状态筛选。 */
    private CourseStatus status;

    /** 审核动作筛选。 */
    private CourseAuditAction auditAction;

    /** 审核意见关键字。 */
    @Size(max = 100, message = "搜索关键字不能超过100个字符")
    private String keyword;
}
