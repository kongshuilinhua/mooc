package com.elysia.mooc.task.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.task.domain.enums.StudyTaskPriorityLevel;
import com.elysia.mooc.task.domain.enums.StudyTaskSourceType;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习任务计划实体，映射 study_task_plan 表。 */
@Data
@TableName("study_task_plan")
public class StudyTaskPlanPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 任务来源类型。 */
    private StudyTaskSourceType sourceType;

    /** 任务来源 ID。 */
    private Long sourceId;

    /** 任务标题。 */
    private String taskTitle;

    /** 计划日期。 */
    private LocalDate planDate;

    /** 优先级。 */
    private StudyTaskPriorityLevel priorityLevel;

    /** 任务状态。 */
    private StudyTaskStatus taskStatus;

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
