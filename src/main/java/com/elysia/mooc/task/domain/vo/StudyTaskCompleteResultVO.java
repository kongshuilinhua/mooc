package com.elysia.mooc.task.domain.vo;

import com.elysia.mooc.task.domain.enums.StudyTaskCompleteStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习任务完成结果。 */
@Data
public class StudyTaskCompleteResultVO {

    /** 任务实例 ID。 */
    private Long instanceId;

    /** 是否已完成。 */
    private Boolean completed;

    /** 完成时间。 */
    private LocalDateTime completedAt;

    /** 实例完成状态。 */
    private StudyTaskCompleteStatus completeStatus;

    /** 计划状态。 */
    private StudyTaskStatus planStatus;

    /** 进度百分比。 */
    private Integer progressPercent;
}
