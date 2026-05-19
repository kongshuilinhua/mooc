package com.elysia.mooc.teaching.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.teaching.domain.enums.TeacherStudentRiskLevel;
import com.elysia.mooc.teaching.domain.vo.TeacherCourseAnalysisVO;
import com.elysia.mooc.teaching.domain.vo.TeacherDashboardOverviewVO;
import com.elysia.mooc.teaching.domain.vo.TeacherStudentProgressVO;
import com.elysia.mooc.teaching.service.TeachingDashboardService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 教师看板接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class TeachingDashboardSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeachingDashboardService teachingDashboardService;

    @Test
    void overviewShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/teacher/dashboard/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldReturn403WhenAccessTeachingDashboard() throws Exception {
        mockMvc.perform(get("/api/teacher/dashboard/overview"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldReturn403WhenAccessTeachingDashboard() throws Exception {
        mockMvc.perform(get("/api/teacher/dashboard/courses/3001/analysis"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldGetOverviewThroughController() throws Exception {
        TeacherDashboardOverviewVO overview = new TeacherDashboardOverviewVO();
        overview.setCourseCount(2);
        overview.setActiveStudentCount(3);
        overview.setAverageCompletionRate(new BigDecimal("42.50"));
        overview.setIncomeAmount(new BigDecimal("199.00"));
        overview.setRefundAmount(BigDecimal.ZERO);
        overview.setPaidOrderCount(1);
        when(teachingDashboardService.getOverview(any())).thenReturn(overview);

        mockMvc.perform(get("/api/teacher/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseCount").value(2))
                .andExpect(jsonPath("$.data.activeStudentCount").value(3))
                .andExpect(jsonPath("$.data.incomeAmount").value(199.00));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldGetCourseAnalysisThroughController() throws Exception {
        TeacherCourseAnalysisVO analysis = new TeacherCourseAnalysisVO();
        analysis.setCourseId(3001L);
        analysis.setCourseName("Java 入门");
        analysis.setViewCount(120);
        analysis.setLearnCount(36);
        analysis.setCompletionRate(new BigDecimal("42.50"));
        analysis.setPaidOrderCount(1);
        analysis.setIncomeAmount(new BigDecimal("199.00"));
        analysis.setRefundAmount(BigDecimal.ZERO);
        when(teachingDashboardService.getCourseAnalysis(eq(3001L), any())).thenReturn(analysis);

        mockMvc.perform(get("/api/teacher/dashboard/courses/3001/analysis")
                        .param("startDate", "2026-05-12")
                        .param("endDate", "2026-05-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseId").value(3001))
                .andExpect(jsonPath("$.data.courseName").value("Java 入门"))
                .andExpect(jsonPath("$.data.paidOrderCount").value(1));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldListStudentsByRiskLevelThroughController() throws Exception {
        TeacherStudentProgressVO item = new TeacherStudentProgressVO();
        item.setStudentId(4L);
        item.setStudentName("学生");
        item.setProgressPercent(new BigDecimal("56.00"));
        item.setLastLearnTime(LocalDateTime.now());
        item.setLatestLearnTime(item.getLastLearnTime());
        item.setRiskLevel(TeacherStudentRiskLevel.RISK);
        item.setRiskLevelDesc("高风险");
        when(teachingDashboardService.listCourseStudents(eq(3001L), any()))
                .thenReturn(PageResult.of(1L, 10, List.of(item)));

        mockMvc.perform(get("/api/teacher/dashboard/courses/3001/students")
                        .param("riskLevel", "RISK")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].studentId").value(4))
                .andExpect(jsonPath("$.data.list[0].riskLevel").value("RISK"));

        verify(teachingDashboardService).listCourseStudents(eq(3001L), any());
    }
}
