package com.elysia.mooc.interaction.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.interaction.constants.InteractionErrorCode;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.QuestionStatus;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import com.elysia.mooc.interaction.domain.vo.LikeResultVO;
import com.elysia.mooc.interaction.domain.vo.QuestionItemVO;
import com.elysia.mooc.interaction.domain.vo.ReportItemVO;
import com.elysia.mooc.interaction.service.InteractionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 互动接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class InteractionControllerWebTest {

    @Mock
    private InteractionService interactionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new InteractionController(interactionService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listQuestionsShouldReturnPageResultAndAcceptStatus() throws Exception {
        when(interactionService.listQuestions(eq(3001L), any()))
                .thenReturn(PageResult.of(1L, 10, List.of(QuestionItemVO.builder()
                        .id(9001L)
                        .courseId(3001L)
                        .title("刷新 Token 为什么要轮换？")
                        .content("登录后刷新令牌为什么不建议一直复用原 token？")
                        .answerCount(1)
                        .likeCount(2)
                        .status(QuestionStatus.OPEN)
                        .createTime(LocalDateTime.now())
                        .build())));

        mockMvc.perform(get("/api/courses/3001/questions")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(9001))
                .andExpect(jsonPath("$.data.list[0].status").value("OPEN"));
    }

    @Test
    void createQuestionShouldReturn400WhenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/courses/3001/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","content":"问题内容"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("问题标题不能为空"));
    }

    @Test
    void acceptAnswerShouldReturn403WhenNotOwner() throws Exception {
        when(interactionService.acceptAnswer(eq(9101L), any()))
                .thenThrow(new BizException(InteractionErrorCode.INTERACTION_FORBIDDEN, "只有提问者可以采纳回答"));

        mockMvc.perform(post("/api/answers/9101/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"questionId":9001}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(InteractionErrorCode.INTERACTION_FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value("只有提问者可以采纳回答"));
    }

    @Test
    void likeShouldAcceptLowercaseTargetTypeAndReturnResult() throws Exception {
        when(interactionService.like(any()))
                .thenReturn(LikeResultVO.builder()
                        .targetType(InteractionTargetType.QUESTION)
                        .targetId(9001L)
                        .currentLikeCount(3)
                        .liked(true)
                        .build());

        mockMvc.perform(post("/api/interactions/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetType":"question","targetId":9001}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetType").value("QUESTION"))
                .andExpect(jsonPath("$.data.currentLikeCount").value(3))
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    void listReportsShouldReturnAdminReportPage() throws Exception {
        when(interactionService.listReports(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(ReportItemVO.builder()
                        .id(9301L)
                        .targetType(InteractionTargetType.QUESTION)
                        .targetId(9001L)
                        .reason("测试举报")
                        .status(ReportStatus.PENDING)
                        .createTime(LocalDateTime.now())
                        .build())));

        mockMvc.perform(get("/api/admin/interactions/reports")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(9301))
                .andExpect(jsonPath("$.data.list[0].status").value("PENDING"));
    }

    @Test
    void handleReportShouldRejectPendingStatus() throws Exception {
        when(interactionService.handleReport(eq(9301L), any()))
                .thenThrow(new BizException(InteractionErrorCode.INTERACTION_PARAM_INVALID, "处理结果不能回退为待处理"));

        mockMvc.perform(put("/api/admin/interactions/reports/9301")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"PENDING","remark":"重新打开"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("处理结果不能回退为待处理"));
    }
}
