package com.elysia.mooc.task.domain.dto;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.task.constants.StudyTaskConstants;
import com.elysia.mooc.task.constants.StudyTaskErrorCode;
import com.elysia.mooc.task.domain.enums.StudyTaskPriorityLevel;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import com.elysia.mooc.task.domain.enums.StudyTaskSourceType;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建学习任务计划请求。 */
@Data
public class CreateStudyTaskPlanRequest implements Checker {

    /** 前端课程 ID，默认映射为 COURSE 来源。 */
    private Long courseId;

    /** 任务标题。 */
    @Size(max = StudyTaskConstants.TASK_TITLE_MAX_LENGTH, message = "任务标题不能超过200个字符")
    private String title;

    /** 前端执行日期，映射为 planDate。 */
    private LocalDate executeDate;

    /** 前端提醒时间，映射为当天提醒时间。 */
    private LocalTime reminderTime;

    /** 兼容后端语义的来源类型。 */
    private StudyTaskSourceType sourceType;

    /** 兼容后端语义的来源 ID。 */
    private Long sourceId;

    /** 兼容 SQL 字段语义的任务标题。 */
    @Size(max = StudyTaskConstants.TASK_TITLE_MAX_LENGTH, message = "任务标题不能超过200个字符")
    private String taskTitle;

    /** 兼容后端语义的计划日期。 */
    private LocalDate planDate;

    /** 任务优先级。 */
    private StudyTaskPriorityLevel priorityLevel = StudyTaskPriorityLevel.NORMAL;

    /** 提醒渠道。 */
    private StudyTaskReminderChannel reminderChannel = StudyTaskReminderChannel.SITE_MESSAGE;

    @Override
    public void check() {
        if (sourceType == null && courseId != null) {
            sourceType = StudyTaskSourceType.COURSE;
        }
        if (sourceId == null) {
            sourceId = courseId;
        }
        if (courseId == null && sourceType == StudyTaskSourceType.COURSE) {
            courseId = sourceId;
        }
        if (planDate == null) {
            planDate = executeDate;
        }
        if (executeDate == null) {
            executeDate = planDate;
        }
        if (!StringUtils.hasText(taskTitle)) {
            taskTitle = trimText(title);
        }
        if (!StringUtils.hasText(title)) {
            title = trimText(taskTitle);
        }
        if (sourceType == null) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "任务来源类型不能为空");
        }
        if (sourceId == null || sourceId <= 0) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "任务来源ID必须为正数");
        }
        if (!StringUtils.hasText(taskTitle)) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "任务标题不能为空");
        }
        if (planDate == null) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "计划日期不能为空");
        }
        if (planDate.isBefore(LocalDate.now())) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "不能创建过去日期的学习任务");
        }
        if (priorityLevel == null) {
            priorityLevel = StudyTaskPriorityLevel.NORMAL;
        }
        if (reminderChannel == null) {
            reminderChannel = StudyTaskReminderChannel.SITE_MESSAGE;
        }
    }

    private String trimText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
