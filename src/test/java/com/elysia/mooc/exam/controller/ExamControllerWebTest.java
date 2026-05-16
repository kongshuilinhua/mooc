package com.elysia.mooc.exam.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.exam.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.exam.domain.dto.ExamQuestionQuery;
import com.elysia.mooc.exam.domain.enums.ExamDifficulty;
import com.elysia.mooc.exam.domain.enums.ExamQuestionType;
import com.elysia.mooc.exam.domain.vo.ExamAnswerRecordVO;
import com.elysia.mooc.exam.domain.vo.ExamRecordVO;
import com.elysia.mooc.exam.domain.vo.PaperVO;
import com.elysia.mooc.exam.domain.vo.QuestionVO;
import com.elysia.mooc.exam.domain.vo.WrongQuestionVO;
import com.elysia.mooc.exam.service.ExamRecordService;
import com.elysia.mooc.exam.service.PaperService;
import com.elysia.mooc.exam.service.QuestionService;
import com.elysia.mooc.exam.service.WrongQuestionService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 考试接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class ExamControllerWebTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private PaperService paperService;

    @Mock
    private ExamRecordService examRecordService;

    @Mock
    private WrongQuestionService wrongQuestionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ExamQuestionController(questionService),
                        new ExamPaperController(paperService),
                        new ExamRecordController(examRecordService),
                        new WrongQuestionController(wrongQuestionService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listQuestionsShouldReturnPageResult() throws Exception {
        QuestionVO question = new QuestionVO();
        question.setId(20001L);
        question.setCourseId(3001L);
        question.setQuestionType(ExamQuestionType.SINGLE);
        question.setStem("JWT 的主要特点是什么？");
        when(questionService.listQuestions(any())).thenReturn(PageResult.of(1L, 10, List.of(question)));

        mockMvc.perform(get("/api/exam/questions?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(20001))
                .andExpect(jsonPath("$.data.list[0].questionType").value("SINGLE"));
    }

    @Test
    void listQuestionsShouldAcceptFrontendDifficultyAlias() throws Exception {
        when(questionService.listQuestions(any())).thenReturn(PageResult.empty(0L, 0));

        mockMvc.perform(get("/api/exam/questions?pageNo=1&pageSize=10&difficulty=INTERMEDIATE"))
                .andExpect(status().isOk());

        ArgumentCaptor<ExamQuestionQuery> captor = ArgumentCaptor.forClass(ExamQuestionQuery.class);
        verify(questionService).listQuestions(captor.capture());
        assertThat(captor.getValue().getDifficulty()).isEqualTo(ExamDifficulty.MEDIUM);
    }

    @Test
    void createQuestionShouldAcceptFrontendAdvancedDifficultyAlias() throws Exception {
        QuestionVO question = new QuestionVO();
        question.setId(20010L);
        when(questionService.createQuestion(any())).thenReturn(question);

        mockMvc.perform(post("/api/exam/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"questionType":"SHORT","stem":"请说明 RBAC 的核心思想","answerText":"角色聚合权限","difficulty":"ADVANCED","score":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(20010));

        ArgumentCaptor<CreateQuestionRequest> captor = ArgumentCaptor.forClass(CreateQuestionRequest.class);
        verify(questionService).createQuestion(captor.capture());
        assertThat(captor.getValue().getDifficulty()).isEqualTo(ExamDifficulty.HARD);
    }

    @Test
    void submitShouldReturnScoreAndAnswerDetails() throws Exception {
        ExamRecordVO record = new ExamRecordVO();
        record.setId(20201L);
        record.setPaperId(20101L);
        record.setUserId(3L);
        record.setScore(new BigDecimal("5.00"));
        record.setPassed(false);
        record.setManualReviewRequired(false);
        record.setAnswers(List.of(ExamAnswerRecordVO.builder()
                .questionId(20002L)
                .answerContent("错误")
                .correct(false)
                .score(BigDecimal.ZERO)
                .teacherComment("自动判分错误")
                .build()));
        when(examRecordService.submit(any())).thenReturn(record);

        mockMvc.perform(post("/api/exam/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"paperId":20101,"answers":[{"questionId":20002,"answer":"错误"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(5.00))
                .andExpect(jsonPath("$.data.manualReviewRequired").value(false))
                .andExpect(jsonPath("$.data.answers[0].correct").value(false));
    }

    @Test
    void listWrongQuestionsShouldReturnCurrentUserPage() throws Exception {
        WrongQuestionVO wrong = new WrongQuestionVO();
        wrong.setId(20301L);
        wrong.setQuestionId(20002L);
        wrong.setWrongCount(2);
        wrong.setResolved(false);
        when(wrongQuestionService.listWrongQuestions(any())).thenReturn(PageResult.of(1L, 10, List.of(wrong)));

        mockMvc.perform(get("/api/exam/wrong-questions?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].wrongCount").value(2))
                .andExpect(jsonPath("$.data.list[0].resolved").value(false));
    }
}
