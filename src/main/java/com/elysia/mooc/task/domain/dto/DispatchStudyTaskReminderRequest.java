package com.elysia.mooc.task.domain.dto;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.task.constants.StudyTaskConstants;
import com.elysia.mooc.task.constants.StudyTaskErrorCode;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 派发学习任务提醒请求。 */
@Data
public class DispatchStudyTaskReminderRequest implements Checker {

    /** 业务日期，前端按日期派发。 */
    private LocalDate bizDate;

    /** 提醒渠道。 */
    private StudyTaskReminderChannel channel = StudyTaskReminderChannel.SITE_MESSAGE;

    /** 兼容后端语义的开始时间。 */
    private LocalDateTime startTime;

    /** 兼容后端语义的结束时间。 */
    private LocalDateTime endTime;

    /** 本批次最多处理数量。 */
    private Integer batchSize = StudyTaskConstants.DEFAULT_BATCH_SIZE;

    @Override
    public void check() {
        if (bizDate == null && startTime == null && endTime == null) {
            bizDate = LocalDate.now();
        }
        if (startTime == null && bizDate != null) {
            startTime = bizDate.atStartOfDay();
        }
        if (endTime == null && bizDate != null) {
            endTime = bizDate.plusDays(1).atStartOfDay();
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_REMINDER_INVALID, "提醒时间窗口不合法");
        }
        if (channel == null) {
            channel = StudyTaskReminderChannel.SITE_MESSAGE;
        }
        if (batchSize == null) {
            batchSize = StudyTaskConstants.DEFAULT_BATCH_SIZE;
        }
        if (batchSize <= 0 || batchSize > StudyTaskConstants.MAX_BATCH_SIZE) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_REMINDER_INVALID, "提醒批次大小必须在1到500之间");
        }
    }
}
