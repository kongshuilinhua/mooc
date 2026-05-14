package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程审核日志实体，映射 course_audit_log 表。 */
@Data
@TableName("course_audit_log")
public class CourseAuditLogPO {

    /** 审核记录 ID。 */
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
