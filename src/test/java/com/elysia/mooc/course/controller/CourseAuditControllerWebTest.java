package com.elysia.mooc.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.course.constants.CourseErrorCode;
import com.elysia.mooc.course.domain.dto.CourseAuditLogQuery;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.vo.CourseAuditLogVO;
import com.elysia.mooc.course.service.CourseAuditService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 课程审核控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class CourseAuditControllerWebTest {

    @Mock
    private CourseAuditService courseAuditService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new CourseAuditController(courseAuditService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitAuditShouldAcceptEmptyBody() throws Exception {
        when(courseAuditService.submitAudit(eq(3006L), any())).thenReturn(true);

        mockMvc.perform(post("/api/courses/3006/submit-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void approveCourseShouldReturn409WhenStatusInvalid() throws Exception {
        when(courseAuditService.approveCourse(eq(3001L), any()))
                .thenThrow(new BizException(CourseErrorCode.COURSE_STATUS_INVALID));

        mockMvc.perform(post("/api/admin/courses/3001/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.COURSE_STATUS_INVALID.code()));
    }

    @Test
    void rejectCourseShouldValidateReason() throws Exception {
        mockMvc.perform(post("/api/admin/courses/3003/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("驳回原因不能为空"));
    }

    @Test
    void offlineCourseShouldValidateReason() throws Exception {
        mockMvc.perform(post("/api/admin/courses/3001/offline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("下架原因不能为空"));
    }

    @Test
    void listAuditLogsShouldReturnPageResultAndBindEnums() throws Exception {
        when(courseAuditService.listAuditLogs(eq(3001L), any(CourseAuditLogQuery.class)))
                .thenReturn(PageResult.of(1L, 10, List.of(CourseAuditLogVO.builder()
                        .id(7001L)
                        .courseId(3001L)
                        .beforeStatus(CourseStatus.DRAFT)
                        .afterStatus(CourseStatus.PENDING)
                        .auditAction(CourseAuditAction.SUBMIT)
                        .auditComment("提交审核")
                        .build())));

        mockMvc.perform(get("/api/courses/3001/audit-logs")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "PENDING")
                        .param("auditAction", "SUBMIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].afterStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.list[0].auditAction").value("SUBMIT"));

        ArgumentCaptor<CourseAuditLogQuery> captor = ArgumentCaptor.forClass(CourseAuditLogQuery.class);
        verify(courseAuditService).listAuditLogs(eq(3001L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(CourseStatus.PENDING);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getAuditAction())
                .isEqualTo(CourseAuditAction.SUBMIT);
    }
}
