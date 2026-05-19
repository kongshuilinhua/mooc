package com.elysia.mooc.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.task.domain.dto.CompleteStudyTaskRequest;
import com.elysia.mooc.task.domain.dto.CreateStudyTaskPlanRequest;
import com.elysia.mooc.task.domain.dto.DispatchStudyTaskReminderRequest;
import com.elysia.mooc.task.domain.enums.StudyTaskCompleteStatus;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import com.elysia.mooc.task.domain.vo.StudyTaskCompleteResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskPlanResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskReminderDispatchResultVO;
import com.elysia.mooc.task.service.StudyTaskService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 学习任务接口合同测试。 */
@ExtendWith(MockitoExtension.class)
class StudyTaskControllerWebTest {

    @Mock
    private StudyTaskService studyTaskService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new StudyTaskController(studyTaskService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTaskPlanShouldReturnFrontendFields() throws Exception {
        StudyTaskPlanResultVO result = new StudyTaskPlanResultVO();
        result.setPlanId(30010L);
        result.setStatus(StudyTaskStatus.PENDING);
        result.setInstanceCount(1);
        result.setFirstInstanceId(30110L);
        when(studyTaskService.createTaskPlan(any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/plans")
                        .contentType("application/json")
                        .content("""
                                {"courseId":3001,"title":"day30学习任务","executeDate":"2026-05-19","reminderTime":"19:30:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(30010))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.instanceCount").value(1))
                .andExpect(jsonPath("$.data.firstInstanceId").value(30110));

        ArgumentCaptor<CreateStudyTaskPlanRequest> captor = ArgumentCaptor.forClass(CreateStudyTaskPlanRequest.class);
        verify(studyTaskService).createTaskPlan(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getCourseId()).isEqualTo(3001L);
    }

    @Test
    void completeTaskShouldAllowEmptyBodyAndReturnCompletedAt() throws Exception {
        StudyTaskCompleteResultVO result = new StudyTaskCompleteResultVO();
        result.setInstanceId(30110L);
        result.setCompleted(true);
        result.setCompletedAt(LocalDateTime.of(2026, 5, 19, 19, 30));
        result.setCompleteStatus(StudyTaskCompleteStatus.DONE);
        when(studyTaskService.completeTaskInstance(eq(30110L), any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/instances/30110/complete")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.instanceId").value(30110))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.completedAt").exists());

        ArgumentCaptor<CompleteStudyTaskRequest> captor = ArgumentCaptor.forClass(CompleteStudyTaskRequest.class);
        verify(studyTaskService).completeTaskInstance(eq(30110L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getProgressPercent()).isEqualTo(100);
    }

    @Test
    void dispatchRemindersShouldReturnBatchResult() throws Exception {
        StudyTaskReminderDispatchResultVO result = new StudyTaskReminderDispatchResultVO();
        result.setBatchId("batch-1");
        result.setReminderCount(2);
        result.setSentCount(2);
        result.setSkippedCount(0);
        result.setFailedCount(0);
        result.setStatus("SUCCESS");
        result.setChannel(StudyTaskReminderChannel.SITE_MESSAGE);
        when(studyTaskService.dispatchReminders(any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/reminders/dispatch")
                        .contentType("application/json")
                        .content("""
                                {"bizDate":"2026-05-19","channel":"SITE_MESSAGE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchId").value("batch-1"))
                .andExpect(jsonPath("$.data.reminderCount").value(2))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        ArgumentCaptor<DispatchStudyTaskReminderRequest> captor = ArgumentCaptor.forClass(DispatchStudyTaskReminderRequest.class);
        verify(studyTaskService).dispatchReminders(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getChannel()).isEqualTo(StudyTaskReminderChannel.SITE_MESSAGE);
    }
}
