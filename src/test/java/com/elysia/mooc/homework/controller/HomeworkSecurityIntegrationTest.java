package com.elysia.mooc.homework.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import com.elysia.mooc.homework.domain.vo.HomeworkAssignmentVO;
import com.elysia.mooc.homework.domain.vo.HomeworkSubmissionVO;
import com.elysia.mooc.homework.service.HomeworkService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 作业接口真实安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class HomeworkSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeworkService homeworkService;

    @Test
    void submitShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/homework/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assignmentId":25001,"submitContent":"作业内容"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void publishShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/homework/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"title":"学生越权发布","deadlineTime":"2099-01-01T10:00:00","allowResubmit":false}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void gradeShouldReturn403WhenStudentAuthenticated() throws Exception {
        mockMvc.perform(post("/api/homework/submissions/25101/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"score":90,"feedback":"越权批改"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldPublishAndStudentShouldSubmitThroughController() throws Exception {
        HomeworkAssignmentVO assignment = new HomeworkAssignmentVO();
        assignment.setId(25001L);
        assignment.setCourseId(3001L);
        assignment.setTitle("JWT 作业");
        assignment.setStatus(HomeworkAssignmentStatus.PUBLISHED);
        assignment.setDeadlineTime(LocalDateTime.of(2099, 1, 1, 10, 0));
        when(homeworkService.publishAssignment(any())).thenReturn(assignment);

        mockMvc.perform(post("/api/homework/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"title":"JWT 作业","deadlineTime":"2099-01-01T10:00:00","allowResubmit":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(25001))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldSubmitThroughController() throws Exception {
        HomeworkSubmissionVO submission = new HomeworkSubmissionVO();
        submission.setId(25101L);
        submission.setSubmissionId(25101L);
        submission.setAssignmentId(25001L);
        submission.setGradeStatus(HomeworkGradeStatus.PENDING);
        when(homeworkService.submitHomework(any())).thenReturn(submission);

        mockMvc.perform(post("/api/homework/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assignmentId":25001,"submitContent":"作业内容"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.submissionId").value(25101))
                .andExpect(jsonPath("$.data.gradeStatus").value("PENDING"));
    }
}
