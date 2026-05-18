package com.elysia.mooc.studyarchive.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteStatus;
import com.elysia.mooc.studyarchive.domain.enums.LearningNoteType;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookMasteryLevel;
import com.elysia.mooc.studyarchive.domain.enums.WrongBookSourceType;
import com.elysia.mooc.studyarchive.domain.vo.LearningNoteVO;
import com.elysia.mooc.studyarchive.domain.vo.LearningReportVO;
import com.elysia.mooc.studyarchive.domain.vo.WrongBookItemVO;
import com.elysia.mooc.studyarchive.service.StudyArchiveService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** 学习档案接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class StudyArchiveSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudyArchiveService studyArchiveService;

    @Test
    void saveNoteShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/study-archive/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"sectionId":5003,"content":"笔记内容"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongBookShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/study-archive/wrong-book"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dailyReportShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/study-archive/reports/daily"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldReturn403WhenAccessStudentArchive() throws Exception {
        mockMvc.perform(get("/api/study-archive/wrong-book"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(CommonErrorCode.FORBIDDEN.message()));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldSaveNoteThroughController() throws Exception {
        LearningNoteVO note = new LearningNoteVO();
        note.setId(26001L);
        note.setCourseId(3001L);
        note.setSectionId(5003L);
        note.setNoteType(LearningNoteType.TEXT);
        note.setStatus(LearningNoteStatus.NORMAL);
        note.setCreateTime(LocalDateTime.now());
        when(studyArchiveService.saveNote(any())).thenReturn(note);

        mockMvc.perform(post("/api/study-archive/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"videoId":5003,"content":"笔记内容","noteType":"TEXT"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(26001))
                .andExpect(jsonPath("$.data.sectionId").value(5003))
                .andExpect(jsonPath("$.data.status").value("NORMAL"));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldListWrongBookThroughController() throws Exception {
        WrongBookItemVO item = new WrongBookItemVO();
        item.setId(26101L);
        item.setQuestionId(20002L);
        item.setSourceType(WrongBookSourceType.EXAM);
        item.setMasteryLevel(WrongBookMasteryLevel.LOW);
        item.setReviewLevel("HIGH");
        when(studyArchiveService.listWrongBook(any())).thenReturn(new PageResult<>(1L, 1, List.of(item)));

        mockMvc.perform(get("/api/study-archive/wrong-book")
                        .param("sourceType", "EXAM")
                        .param("masteryLevel", "LOW")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].sourceType").value("EXAM"));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldGetDailyReportThroughController() throws Exception {
        LearningReportVO report = new LearningReportVO();
        report.setReportDate(LocalDate.of(2026, 5, 18));
        report.setStudyMinutes(68);
        report.setLearningMinutes(68);
        report.setCompletedSections(2);
        report.setWrongCount(1);
        report.setAiAskCount(3);
        report.setSource("PERSISTED");
        when(studyArchiveService.getDailyReport(any())).thenReturn(report);

        mockMvc.perform(get("/api/study-archive/reports/daily")
                        .param("bizDate", "2026-05-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reportDate").value("2026-05-18"))
                .andExpect(jsonPath("$.data.studyMinutes").value(68))
                .andExpect(jsonPath("$.data.learningMinutes").value(68))
                .andExpect(jsonPath("$.data.source").value("PERSISTED"));
    }
}
