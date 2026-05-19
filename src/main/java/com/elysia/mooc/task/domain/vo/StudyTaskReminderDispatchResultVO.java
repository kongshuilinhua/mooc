package com.elysia.mooc.task.domain.vo;

import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习任务提醒派发结果。 */
@Data
public class StudyTaskReminderDispatchResultVO {

    /** 派发批次 ID。 */
    private String batchId;

    /** 命中提醒数量。 */
    private Integer reminderCount;

    /** 已发送数量。 */
    private Integer sentCount;

    /** 跳过数量。 */
    private Integer skippedCount;

    /** 失败数量。 */
    private Integer failedCount;

    /** 派发状态。 */
    private String status;

    /** 提醒渠道。 */
    private StudyTaskReminderChannel channel;

    /** 派发时间。 */
    private LocalDateTime dispatchedAt;
}
