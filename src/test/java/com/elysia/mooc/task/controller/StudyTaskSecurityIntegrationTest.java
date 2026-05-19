package com.elysia.mooc.task.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.task.domain.enums.StudyTaskStatus;
import com.elysia.mooc.task.domain.vo.StudyTaskCompleteResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskPlanResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskReminderDispatchResultVO;
import com.elysia.mooc.task.service.StudyTaskService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 学习任务接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class StudyTaskSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyTaskService studyTaskService;

    @Test
    void createPlanShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/study/tasks/plans")
                        .contentType("application/json")
                        .content("""
                                {"courseId":3001,"title":"匿名测试","executeDate":"2026-05-19"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldReturn403WhenCreateStudentTask() throws Exception {
        mockMvc.perform(post("/api/study/tasks/plans")
                        .contentType("application/json")
                        .content("""
                                {"courseId":3001,"title":"教师越权","executeDate":"2026-05-19"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldCreatePlanThroughController() throws Exception {
        StudyTaskPlanResultVO result = new StudyTaskPlanResultVO();
        result.setPlanId(30010L);
        result.setStatus(StudyTaskStatus.PENDING);
        result.setInstanceCount(1);
        org.mockito.Mockito.when(studyTaskService.createTaskPlan(any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/plans")
                        .contentType("application/json")
                        .content("""
                                {"courseId":3001,"title":"学生任务","executeDate":"2026-05-19"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(30010));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldCompleteOwnTaskThroughController() throws Exception {
        StudyTaskCompleteResultVO result = new StudyTaskCompleteResultVO();
        result.setInstanceId(30110L);
        result.setCompleted(true);
        result.setCompletedAt(LocalDateTime.now());
        org.mockito.Mockito.when(studyTaskService.completeTaskInstance(eq(30110L), any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/instances/30110/complete")
                        .contentType("application/json")
                        .content("""
                                {"progressPercent":100,"completeNote":"完成"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldReturn403WhenDispatchReminder() throws Exception {
        mockMvc.perform(post("/api/study/tasks/reminders/dispatch")
                        .contentType("application/json")
                        .content("""
                                {"bizDate":"2026-05-19","channel":"SITE_MESSAGE"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldDispatchReminderThroughController() throws Exception {
        StudyTaskReminderDispatchResultVO result = new StudyTaskReminderDispatchResultVO();
        result.setBatchId("batch-1");
        result.setReminderCount(1);
        result.setStatus("SUCCESS");
        org.mockito.Mockito.when(studyTaskService.dispatchReminders(any())).thenReturn(result);

        mockMvc.perform(post("/api/study/tasks/reminders/dispatch")
                        .contentType("application/json")
                        .content("""
                                {"bizDate":"2026-05-19","channel":"SITE_MESSAGE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchId").value("batch-1"))
                .andExpect(jsonPath("$.data.reminderCount").value(1));
    }
}
