package com.elysia.mooc.task.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.task.constants.StudyTaskErrorCode;
import com.elysia.mooc.task.domain.dto.CompleteStudyTaskRequest;
import com.elysia.mooc.task.domain.dto.CreateStudyTaskPlanRequest;
import com.elysia.mooc.task.domain.dto.DispatchStudyTaskReminderRequest;
import com.elysia.mooc.task.domain.enums.StudyTaskCompleteStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskSourceType;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 学习任务服务测试。 */
@ExtendWith(MockitoExtension.class)
class StudyTaskServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private StudyTaskPlanMapper taskPlanMapper;

    @Mock
    private StudyTaskInstanceMapper taskInstanceMapper;

    @Mock
    private StudyTaskReminderMapper taskReminderMapper;

    @InjectMocks
    private StudyTaskServiceImpl studyTaskService;

    @Test
    void createTaskPlanShouldInsertPlanInstanceAndReminder() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(taskPlanMapper.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            StudyTaskPlanPO plan = invocation.getArgument(0);
            plan.setId(30010L);
            return 1;
        }).when(taskPlanMapper).insert(any(StudyTaskPlanPO.class));
        doAnswer(invocation -> {
            StudyTaskInstancePO instance = invocation.getArgument(0);
            instance.setId(30110L);
            return 1;
        }).when(taskInstanceMapper).insert(any(StudyTaskInstancePO.class));
        doAnswer(invocation -> {
            StudyTaskReminderPO reminder = invocation.getArgument(0);
            reminder.setId(30210L);
            return 1;
        }).when(taskReminderMapper).insert(any(StudyTaskReminderPO.class));

        CreateStudyTaskPlanRequest request = new CreateStudyTaskPlanRequest();
        request.setCourseId(3001L);
        request.setTitle("day30学习任务");
        request.setExecuteDate(LocalDate.now());

        StudyTaskPlanResultVO result = studyTaskService.createTaskPlan(request);

        assertThat(result.getPlanId()).isEqualTo(30010L);
        assertThat(result.getFirstInstanceId()).isEqualTo(30110L);
        assertThat(result.getStatus()).isEqualTo(StudyTaskStatus.PENDING);
        ArgumentCaptor<StudyTaskPlanPO> planCaptor = ArgumentCaptor.forClass(StudyTaskPlanPO.class);
        verify(taskPlanMapper).insert(planCaptor.capture());
        assertThat(planCaptor.getValue().getUserId()).isEqualTo(4L);
        assertThat(planCaptor.getValue().getSourceType()).isEqualTo(StudyTaskSourceType.COURSE);
        verify(taskInstanceMapper).insert(any(StudyTaskInstancePO.class));
        verify(taskReminderMapper).insert(any(StudyTaskReminderPO.class));
    }

    @Test
    void createTaskPlanShouldRejectDuplicatedPlan() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(taskPlanMapper.selectCount(any())).thenReturn(1L);

        CreateStudyTaskPlanRequest request = new CreateStudyTaskPlanRequest();
        request.setCourseId(3001L);
        request.setTitle("重复任务");
        request.setExecuteDate(LocalDate.now());

        assertThatThrownBy(() -> studyTaskService.createTaskPlan(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyTaskErrorCode.STUDY_TASK_PLAN_DUPLICATED.code());
        verify(taskPlanMapper, never()).insert(any(StudyTaskPlanPO.class));
    }

    @Test
    void completeTaskShouldUpdateInstanceAndPlan() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        StudyTaskInstancePO instance = instance(30110L, 30010L, StudyTaskCompleteStatus.TODO);
        StudyTaskPlanPO plan = plan(30010L, 4L, StudyTaskStatus.PENDING);
        when(taskInstanceMapper.selectById(30110L)).thenReturn(instance);
        when(taskPlanMapper.selectById(30010L)).thenReturn(plan);

        CompleteStudyTaskRequest request = new CompleteStudyTaskRequest();
        request.setProgressPercent(80);

        StudyTaskCompleteResultVO result = studyTaskService.completeTaskInstance(30110L, request);

        assertThat(result.getCompleted()).isTrue();
        assertThat(result.getPlanStatus()).isEqualTo(StudyTaskStatus.FINISHED);
        verify(taskInstanceMapper).updateById(instance);
        verify(taskPlanMapper).updateById(plan);
    }

    @Test
    void completeTaskShouldReturnCurrentResultWhenAlreadyDone() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        StudyTaskInstancePO instance = instance(30110L, 30010L, StudyTaskCompleteStatus.DONE);
        instance.setCompleteTime(LocalDateTime.of(2026, 5, 19, 19, 30));
        StudyTaskPlanPO plan = plan(30010L, 4L, StudyTaskStatus.FINISHED);
        when(taskInstanceMapper.selectById(30110L)).thenReturn(instance);
        when(taskPlanMapper.selectById(30010L)).thenReturn(plan);

        StudyTaskCompleteResultVO result = studyTaskService.completeTaskInstance(30110L, new CompleteStudyTaskRequest());

        assertThat(result.getCompleted()).isTrue();
        assertThat(result.getCompletedAt()).isEqualTo(LocalDateTime.of(2026, 5, 19, 19, 30));
        verify(taskInstanceMapper, never()).updateById(any(StudyTaskInstancePO.class));
        verify(taskPlanMapper, never()).updateById(any(StudyTaskPlanPO.class));
    }

    @Test
    void completeTaskShouldRejectOtherStudentInstance() {
        when(userContextService.currentLoginUser()).thenReturn(student());
        when(taskInstanceMapper.selectById(30110L)).thenReturn(instance(30110L, 30010L, StudyTaskCompleteStatus.TODO));
        when(taskPlanMapper.selectById(30010L)).thenReturn(plan(30010L, 99L, StudyTaskStatus.PENDING));

        assertThatThrownBy(() -> studyTaskService.completeTaskInstance(30110L, new CompleteStudyTaskRequest()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyTaskErrorCode.STUDY_TASK_FORBIDDEN.code());
    }

    @Test
    void dispatchRemindersShouldSendPendingAndSkipCompletedInstances() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        StudyTaskReminderPO pendingReminder = reminder(30210L, 30110L);
        StudyTaskReminderPO completedReminder = reminder(30211L, 30111L);
        when(taskReminderMapper.selectList(any())).thenReturn(List.of(pendingReminder, completedReminder));
        when(taskInstanceMapper.selectById(30110L)).thenReturn(instance(30110L, 30010L, StudyTaskCompleteStatus.TODO));
        when(taskInstanceMapper.selectById(30111L)).thenReturn(instance(30111L, 30011L, StudyTaskCompleteStatus.DONE));

        DispatchStudyTaskReminderRequest request = new DispatchStudyTaskReminderRequest();
        request.setBizDate(LocalDate.now());
        request.setChannel(StudyTaskReminderChannel.SITE_MESSAGE);

        StudyTaskReminderDispatchResultVO result = studyTaskService.dispatchReminders(request);

        assertThat(result.getReminderCount()).isEqualTo(2);
        assertThat(result.getSentCount()).isEqualTo(1);
        assertThat(result.getSkippedCount()).isEqualTo(1);
        assertThat(pendingReminder.getSendStatus()).isEqualTo(StudyTaskReminderStatus.SENT);
        assertThat(completedReminder.getSendStatus()).isEqualTo(StudyTaskReminderStatus.SKIPPED);
        verify(taskReminderMapper).updateById(pendingReminder);
        verify(taskReminderMapper).updateById(completedReminder);
    }

    @Test
    void teacherShouldNotDispatchReminders() {
        when(userContextService.currentLoginUser()).thenReturn(new LoginUser(2L, "teacher", List.of("TEACHER"), List.of()));

        DispatchStudyTaskReminderRequest request = new DispatchStudyTaskReminderRequest();

        assertThatThrownBy(() -> studyTaskService.dispatchReminders(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(StudyTaskErrorCode.STUDY_TASK_FORBIDDEN.code());
    }

    private LoginUser student() {
        return new LoginUser(4L, "student", List.of("STUDENT"), List.of("learning:view"));
    }

    private LoginUser admin() {
        return new LoginUser(1L, "admin", List.of("ADMIN"), List.of());
    }

    private StudyTaskPlanPO plan(Long id, Long userId, StudyTaskStatus status) {
        StudyTaskPlanPO plan = new StudyTaskPlanPO();
        plan.setId(id);
        plan.setUserId(userId);
        plan.setTaskStatus(status);
        return plan;
    }

    private StudyTaskInstancePO instance(Long id, Long planId, StudyTaskCompleteStatus status) {
        StudyTaskInstancePO instance = new StudyTaskInstancePO();
        instance.setId(id);
        instance.setPlanId(planId);
        instance.setScheduleDate(LocalDate.now());
        instance.setCompleteStatus(status);
        instance.setProgressPercent(status == StudyTaskCompleteStatus.DONE ? 100 : 0);
        return instance;
    }

    private StudyTaskReminderPO reminder(Long id, Long instanceId) {
        StudyTaskReminderPO reminder = new StudyTaskReminderPO();
        reminder.setId(id);
        reminder.setInstanceId(instanceId);
        reminder.setRemindChannel(StudyTaskReminderChannel.SITE_MESSAGE);
        reminder.setRemindTime(LocalDateTime.now());
        reminder.setSendStatus(StudyTaskReminderStatus.PENDING);
        reminder.setRetryCount(0);
        return reminder;
    }
}
