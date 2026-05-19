package com.elysia.mooc.task.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.task.domain.enums.StudyTaskCompleteStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习任务实例实体，映射 study_task_instance 表。 */
@Data
@TableName("study_task_instance")
public class StudyTaskInstancePO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务计划 ID。 */
    private Long planId;

    /** 执行日期。 */
    private LocalDate scheduleDate;

    /** 完成时间。 */
    private LocalDateTime completeTime;

    /** 完成状态。 */
    private StudyTaskCompleteStatus completeStatus;

    /** 进度百分比。 */
    private Integer progressPercent;

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
