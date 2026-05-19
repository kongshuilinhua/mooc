package com.elysia.mooc.task.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.task.constants.StudyTaskConstants;
import com.elysia.mooc.task.constants.StudyTaskErrorCode;
import com.elysia.mooc.task.domain.dto.CompleteStudyTaskRequest;
import com.elysia.mooc.task.domain.dto.CreateStudyTaskPlanRequest;
import com.elysia.mooc.task.domain.dto.DispatchStudyTaskReminderRequest;
import com.elysia.mooc.task.domain.enums.StudyTaskCompleteStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import com.elysia.mooc.task.domain.po.StudyTaskInstancePO;
import com.elysia.mooc.task.domain.po.StudyTaskPlanPO;
import com.elysia.mooc.task.domain.po.StudyTaskReminderPO;
import com.elysia.mooc.task.domain.vo.StudyTaskCompleteResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskPlanResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskReminderDispatchResultVO;
import com.elysia.mooc.task.mapper.StudyTaskInstanceMapper;
import com.elysia.mooc.task.mapper.StudyTaskPlanMapper;
import com.elysia.mooc.task.mapper.StudyTaskReminderMapper;
import com.elysia.mooc.task.service.StudyTaskService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 学习任务服务实现。 */
@Service
@RequiredArgsConstructor
public class StudyTaskServiceImpl implements StudyTaskService {

    private final UserContextService userContextService;
    private final StudyTaskPlanMapper taskPlanMapper;
    private final StudyTaskInstanceMapper taskInstanceMapper;
    private final StudyTaskReminderMapper taskReminderMapper;

    /**
     * 创建当前学生的学习任务计划。
     *
     * @param request 创建任务请求
     * @return 任务计划创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTaskPlanResultVO createTaskPlan(CreateStudyTaskPlanRequest request) {
        LoginUser loginUser = requireStudent();
        request.check();
        ensureNoDuplicatedPlan(loginUser.getUserId(), request);

        StudyTaskPlanPO plan = new StudyTaskPlanPO();
        plan.setUserId(loginUser.getUserId());
        plan.setSourceType(request.getSourceType());
        plan.setSourceId(request.getSourceId());
        plan.setTaskTitle(request.getTaskTitle());
        plan.setPlanDate(request.getPlanDate());
        plan.setPriorityLevel(request.getPriorityLevel());
        plan.setTaskStatus(StudyTaskStatus.PENDING);
        plan.setDeleted(0);
        taskPlanMapper.insert(plan);

        StudyTaskInstancePO instance = new StudyTaskInstancePO();
        instance.setPlanId(plan.getId());
        instance.setScheduleDate(plan.getPlanDate());
        instance.setCompleteStatus(StudyTaskCompleteStatus.TODO);
        instance.setProgressPercent(0);
        instance.setDeleted(0);
        taskInstanceMapper.insert(instance);

        StudyTaskReminderPO reminder = new StudyTaskReminderPO();
        reminder.setInstanceId(instance.getId());
        reminder.setRemindChannel(request.getReminderChannel());
        reminder.setRemindTime(plan.getPlanDate().atTime(resolveReminderTime(request)));
        reminder.setSendStatus(StudyTaskReminderStatus.PENDING);
        reminder.setRetryCount(0);
        reminder.setDeleted(0);
        taskReminderMapper.insert(reminder);

        return toPlanResult(plan, instance);
    }

    /**
     * 完成当前学生的学习任务实例。
     *
     * @param instanceId 任务实例 ID
     * @param request 完成任务请求
     * @return 完成结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTaskCompleteResultVO completeTaskInstance(Long instanceId, CompleteStudyTaskRequest request) {
        LoginUser loginUser = requireStudent();
        request.check();
        StudyTaskInstancePO instance = requireInstance(instanceId);
        StudyTaskPlanPO plan = requirePlan(instance.getPlanId());
        if (!Objects.equals(plan.getUserId(), loginUser.getUserId())) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_FORBIDDEN);
        }

        // 已完成任务不重复累计进度或触发下游奖励，直接返回当前完成状态保证前端重试幂等。
        if (instance.getCompleteStatus() != StudyTaskCompleteStatus.DONE) {
            LocalDateTime completeTime = request.getCompleteTime() == null ? LocalDateTime.now() : request.getCompleteTime();
            instance.setCompleteStatus(StudyTaskCompleteStatus.DONE);
            instance.setCompleteTime(completeTime);
            instance.setProgressPercent(Math.max(request.getProgressPercent(), 100));
            taskInstanceMapper.updateById(instance);

            plan.setTaskStatus(StudyTaskStatus.FINISHED);
            taskPlanMapper.updateById(plan);
        }
        return toCompleteResult(instance, plan);
    }

    /**
     * 派发待发送学习任务提醒。
     *
     * @param request 提醒派发请求
     * @return 派发结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTaskReminderDispatchResultVO dispatchReminders(DispatchStudyTaskReminderRequest request) {
        requireDispatchPermission();
        request.check();
        List<StudyTaskReminderPO> reminders = taskReminderMapper.selectList(Wrappers.<StudyTaskReminderPO>lambdaQuery()
                .eq(StudyTaskReminderPO::getSendStatus, StudyTaskReminderStatus.PENDING)
                .eq(StudyTaskReminderPO::getRemindChannel, request.getChannel())
                .ge(StudyTaskReminderPO::getRemindTime, request.getStartTime())
                .lt(StudyTaskReminderPO::getRemindTime, request.getEndTime())
                .orderByAsc(StudyTaskReminderPO::getRemindTime)
                .last("LIMIT " + request.getBatchSize()));

        int sentCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        for (StudyTaskReminderPO reminder : reminders) {
            StudyTaskInstancePO instance = taskInstanceMapper.selectById(reminder.getInstanceId());
            if (instance == null || instance.getCompleteStatus() == StudyTaskCompleteStatus.DONE) {
                skippedCount++;
                markReminder(reminder, StudyTaskReminderStatus.SKIPPED, "任务已完成或不存在，跳过提醒");
                continue;
            }
            sentCount++;
            markReminder(reminder, StudyTaskReminderStatus.SENT, "提醒已派发到" + request.getChannel().getDesc());
        }

        StudyTaskReminderDispatchResultVO result = new StudyTaskReminderDispatchResultVO();
        result.setBatchId(UUID.randomUUID().toString());
        result.setReminderCount(reminders.size());
        result.setSentCount(sentCount);
        result.setSkippedCount(skippedCount);
        result.setFailedCount(failedCount);
        result.setStatus(failedCount > 0 ? "PARTIAL_FAILED" : "SUCCESS");
        result.setChannel(request.getChannel());
        result.setDispatchedAt(LocalDateTime.now());
        return result;
    }

    private void ensureNoDuplicatedPlan(Long userId, CreateStudyTaskPlanRequest request) {
        Long count = taskPlanMapper.selectCount(Wrappers.<StudyTaskPlanPO>lambdaQuery()
                .eq(StudyTaskPlanPO::getUserId, userId)
                .eq(StudyTaskPlanPO::getSourceType, request.getSourceType())
                .eq(StudyTaskPlanPO::getSourceId, request.getSourceId())
                .eq(StudyTaskPlanPO::getPlanDate, request.getPlanDate())
                .eq(StudyTaskPlanPO::getDeleted, 0));
        if (count != null && count > 0) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PLAN_DUPLICATED);
        }
    }

    private StudyTaskInstancePO requireInstance(Long instanceId) {
        if (instanceId == null || instanceId <= 0) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PARAM_INVALID, "任务实例ID必须为正数");
        }
        StudyTaskInstancePO instance = taskInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_INSTANCE_NOT_FOUND);
        }
        return instance;
    }

    private StudyTaskPlanPO requirePlan(Long planId) {
        StudyTaskPlanPO plan = planId == null ? null : taskPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(StudyTaskErrorCode.STUDY_TASK_PLAN_NOT_FOUND);
        }
        return plan;
    }

    private void markReminder(StudyTaskReminderPO reminder, StudyTaskReminderStatus status, String result) {
        reminder.setSendStatus(status);
        reminder.setSendResult(result);
        reminder.setRetryCount(safeInt(reminder.getRetryCount()) + 1);
        taskReminderMapper.updateById(reminder);
    }

    private java.time.LocalTime resolveReminderTime(CreateStudyTaskPlanRequest request) {
        if (request.getReminderTime() != null) {
            return request.getReminderTime();
        }
        return java.time.LocalTime.parse(StudyTaskConstants.DEFAULT_REMINDER_TIME);
    }

    private StudyTaskPlanResultVO toPlanResult(StudyTaskPlanPO plan, StudyTaskInstancePO instance) {
        StudyTaskPlanResultVO result = new StudyTaskPlanResultVO();
        result.setPlanId(plan.getId());
        result.setStatus(plan.getTaskStatus());
        result.setInstanceCount(1);
        result.setFirstInstanceId(instance.getId());
        result.setSourceType(plan.getSourceType());
        result.setSourceId(plan.getSourceId());
        result.setTitle(plan.getTaskTitle());
        result.setPlanDate(plan.getPlanDate());
        result.setPriorityLevel(plan.getPriorityLevel());
        return result;
    }

    private StudyTaskCompleteResultVO toCompleteResult(StudyTaskInstancePO instance, StudyTaskPlanPO plan) {
        StudyTaskCompleteResultVO result = new StudyTaskCompleteResultVO();
        result.setInstanceId(instance.getId());
        result.setCompleted(instance.getCompleteStatus() == StudyTaskCompleteStatus.DONE);
        result.setCompletedAt(instance.getCompleteTime());
        result.setCompleteStatus(instance.getCompleteStatus());
        result.setPlanStatus(plan.getTaskStatus());
        result.setProgressPercent(instance.getProgressPercent());
        return result;
    }

    private LoginUser requireStudent() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, StudyTaskConstants.ROLE_STUDENT)) {
            return loginUser;
        }
        throw new BizException(StudyTaskErrorCode.STUDY_TASK_FORBIDDEN);
    }

    private LoginUser requireDispatchPermission() {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (hasRole(loginUser, StudyTaskConstants.ROLE_ADMIN)
                || hasPermission(loginUser, StudyTaskConstants.PERMISSION_TASK_DISPATCH)) {
            return loginUser;
        }
        throw new BizException(StudyTaskErrorCode.STUDY_TASK_FORBIDDEN);
    }

    private boolean hasRole(LoginUser loginUser, String roleCode) {
        return loginUser != null
                && loginUser.getRoles() != null
                && loginUser.getRoles().stream().anyMatch(roleCode::equalsIgnoreCase);
    }

    private boolean hasPermission(LoginUser loginUser, String permission) {
        return loginUser != null
                && loginUser.getPermissions() != null
                && loginUser.getPermissions().stream().anyMatch(permission::equalsIgnoreCase);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
