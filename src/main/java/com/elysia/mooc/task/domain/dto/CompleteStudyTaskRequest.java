package com.elysia.mooc.task.domain.dto;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.task.constants.StudyTaskConstants;
import com.elysia.mooc.task.constants.StudyTaskErrorCode;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/** 完成学习任务请求。 */
@Data
public class CompleteStudyTaskRequest implements Checker {

    /** 完成进度百分比。 */
    private Integer progressPercent = 100;

    /** 完成时间，不传时使用当前时间。 */
    private LocalDateTime completeTime;

    /** 完成说明，当前不单独落库，仅用于后续扩展和接口兼容。 */
    @Size(max = StudyTaskConstants.COMPLETE_NOTE_MAX_LENGTH, message = "完成说明不能超过500个字符")
    private String completeNote;

    @Override
    public void check() {
        if (progressPercent == null) {
            progressPercent = 100;
        }
        if (progressPercent < 0 || progressPercent > 100) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "完成进度必须在0到100之间");
        }
    }
}
