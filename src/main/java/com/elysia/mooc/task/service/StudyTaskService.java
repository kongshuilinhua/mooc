package com.elysia.mooc.task.service;

import com.elysia.mooc.task.domain.dto.CompleteStudyTaskRequest;
import com.elysia.mooc.task.domain.dto.CreateStudyTaskPlanRequest;
import com.elysia.mooc.task.domain.dto.DispatchStudyTaskReminderRequest;
import com.elysia.mooc.task.domain.vo.StudyTaskCompleteResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskPlanResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskReminderDispatchResultVO;

/** 学习任务服务。 */
public interface StudyTaskService {

    /**
     * 创建当前学生的学习任务计划。
     *
     * @param request 创建任务请求
     * @return 任务计划创建结果
     */
    StudyTaskPlanResultVO createTaskPlan(CreateStudyTaskPlanRequest request);

    /**
     * 完成当前学生的学习任务实例。
     *
     * @param instanceId 任务实例 ID
     * @param request 完成任务请求
     * @return 完成结果
     */
    StudyTaskCompleteResultVO completeTaskInstance(Long instanceId, CompleteStudyTaskRequest request);

    /**
     * 派发待发送学习任务提醒。
     *
     * @param request 提醒派发请求
     * @return 派发结果
     */
    StudyTaskReminderDispatchResultVO dispatchReminders(DispatchStudyTaskReminderRequest request);
}
