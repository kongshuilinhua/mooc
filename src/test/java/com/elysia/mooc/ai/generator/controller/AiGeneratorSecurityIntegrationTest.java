package com.elysia.mooc.ai.generator.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.generator.domain.enums.AiGenerationStatus;
import com.elysia.mooc.ai.generator.domain.enums.AiLearningPathStatus;
import com.elysia.mooc.ai.generator.domain.enums.AiQuestionReviewStatus;
import com.elysia.mooc.ai.generator.domain.vo.ChapterSummaryResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionDraftVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionsResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathStageVO;
import com.elysia.mooc.ai.generator.service.AiGeneratorService;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** AI 生成接口安全链路测试。 */
@SpringBootTest(properties = {
        "mooc.event.message-consumer-auto-startup=false",
        "mooc.qdrant.auto-initialize=false"
})
@AutoConfigureMockMvc
class AiGeneratorSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiGeneratorService aiGeneratorService;

    @Test
    void summaryShouldReturn401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/ai/generator/chapters/4002/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldReturn403WhenGenerateSummary() throws Exception {
        mockMvc.perform(post("/api/ai/generator/chapters/4002/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldGenerateSummary() throws Exception {
        ChapterSummaryResultVO result = new ChapterSummaryResultVO();
        result.setTaskId(28001L);
        result.setChapterId(4002L);
        result.setSummary("章节总结");
        result.setKeyPoints(List.of("JWT", "RBAC"));
        result.setStatus(AiGenerationStatus.SUCCESS);
        when(aiGeneratorService.generateChapterSummary(eq(4002L), any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/generator/chapters/4002/summary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(28001))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminShouldReturn403WhenGenerateQuestions() throws Exception {
        mockMvc.perform(post("/api/ai/generator/courses/3001/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionCount\":2,\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldGenerateQuestionDrafts() throws Exception {
        GeneratedQuestionDraftVO draft = new GeneratedQuestionDraftVO();
        draft.setDraftId(28101L);
        draft.setType(ExamQuestionType.SINGLE);
        draft.setStem("题干");
        draft.setOptions(List.of("A", "B"));
        draft.setAnswer("A");
        draft.setAnalysis("解析");
        draft.setDifficulty(ExamDifficulty.MEDIUM);
        draft.setReviewStatus(AiQuestionReviewStatus.PENDING);
        GeneratedQuestionsResultVO result = new GeneratedQuestionsResultVO();
        result.setTaskId(28002L);
        result.setCourseId(3001L);
        result.setQuestionCount(1);
        result.setReviewStatus(AiQuestionReviewStatus.PENDING);
        result.setQuestions(List.of(draft));
        result.setStatus(AiGenerationStatus.SUCCESS);
        when(aiGeneratorService.generateQuestionDrafts(eq(3001L), any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/generator/courses/3001/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionCount\":1,\"difficulty\":\"MEDIUM\",\"questionType\":\"SINGLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.questions[0].draftId").value(28101));
    }

    @Test
    @WithMockUser(username = "teacher", roles = "TEACHER")
    void teacherShouldReturn403WhenGenerateLearningPath() throws Exception {
        mockMvc.perform(post("/api/ai/generator/students/4/learning-path")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"补齐基础\",\"horizonDays\":30}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CommonErrorCode.FORBIDDEN.code()));
    }

    @Test
    @WithMockUser(username = "student", roles = "STUDENT")
    void studentShouldGenerateLearningPath() throws Exception {
        LearningPathResultVO result = new LearningPathResultVO();
        result.setPathId(28201L);
        result.setStudentId(4L);
        result.setTheme("个性化学习路径");
        result.setDailyMinutes(45);
        result.setStatus(AiLearningPathStatus.ACTIVE);
        result.setStages(List.of(LearningPathStageVO.builder()
                .stageName("阶段一")
                .goal("补齐基础")
                .durationDays(10)
                .tasks(List.of("复盘课程"))
                .build()));
        when(aiGeneratorService.generateLearningPath(eq(4L), any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/generator/students/4/learning-path")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"补齐基础\",\"horizonDays\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pathId").value(28201))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.stages[0].stageName").value("阶段一"));
    }
}
