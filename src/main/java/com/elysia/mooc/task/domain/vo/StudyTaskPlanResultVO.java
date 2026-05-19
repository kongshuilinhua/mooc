package com.elysia.mooc.task.domain.vo;

import com.elysia.mooc.task.domain.enums.StudyTaskPriorityLevel;
import com.elysia.mooc.task.domain.enums.StudyTaskSourceType;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import java.time.LocalDate;
import lombok.Data;

/** 学习任务计划创建结果。 */
@Data
public class StudyTaskPlanResultVO {

    /** 任务计划 ID。 */
    private Long planId;

    /** 计划状态。 */
    private StudyTaskStatus status;

    /** 生成的任务实例数量。 */
    private Integer instanceCount;

    /** 首个任务实例 ID，便于前端继续完成打卡。 */
    private Long firstInstanceId;

    /** 任务来源类型。 */
    private StudyTaskSourceType sourceType;

    /** 任务来源 ID。 */
    private Long sourceId;

    /** 任务标题。 */
    private String title;

    /** 计划日期。 */
    private LocalDate planDate;

    /** 优先级。 */
    private StudyTaskPriorityLevel priorityLevel;
}
