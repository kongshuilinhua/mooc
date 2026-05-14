package com.elysia.mooc.learning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.learning.constants.LearningErrorCode;
import com.elysia.mooc.learning.domain.enums.LearningCourseStatus;
import com.elysia.mooc.learning.domain.vo.LearningCourseItem;
import com.elysia.mooc.learning.domain.vo.LearningRecordVO;
import com.elysia.mooc.learning.service.LearningService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 学习接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class LearningControllerWebTest {

    @Mock
    private LearningService learningService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LearningController(learningService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void heartbeatShouldReturn400WhenPositionGreaterThanDuration() throws Exception {
        when(learningService.heartbeat(any()))
                .thenThrow(new BizException(LearningErrorCode.LEARNING_PARAM_INVALID, "播放位置不能超过视频总时长"));

        mockMvc.perform(post("/api/learning/records/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"sectionId":5001,"position":901,"duration":900}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(LearningErrorCode.LEARNING_PARAM_INVALID.code()))
                .andExpect(jsonPath("$.message").value("播放位置不能超过视频总时长"));
    }

    @Test
    void heartbeatShouldReturn409WhenCourseNotJoined() throws Exception {
        when(learningService.heartbeat(any()))
                .thenThrow(new BizException(LearningErrorCode.LEARNING_COURSE_NOT_JOINED));

        mockMvc.perform(post("/api/learning/records/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"sectionId":5001,"position":120,"duration":900}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(LearningErrorCode.LEARNING_COURSE_NOT_JOINED.code()))
                .andExpect(jsonPath("$.message").value(LearningErrorCode.LEARNING_COURSE_NOT_JOINED.message()));
    }

    @Test
    void heartbeatShouldReturnRecordContract() throws Exception {
        when(learningService.heartbeat(any()))
                .thenReturn(LearningRecordVO.builder()
                        .courseId(3001L)
                        .sectionId(5001L)
                        .videoId(5001L)
                        .lastPlayTime(120)
                        .maxHistoryTime(120)
                        .completed(false)
                        .position(120)
                        .duration(900)
                        .build());

        mockMvc.perform(post("/api/learning/records/heartbeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":3001,"sectionId":5001,"position":120,"duration":900}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sectionId").value(5001))
                .andExpect(jsonPath("$.data.lastPlayTime").value(120))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    void listMyCoursesShouldReturnPageResult() throws Exception {
        when(learningService.listMyCourses(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(LearningCourseItem.builder()
                        .courseId(3001L)
                        .courseName("Spring Boot 3 实战入门")
                        .progressPercent(new BigDecimal("50.00"))
                        .learnedSeconds(1200)
                        .status(LearningCourseStatus.LEARNING)
                        .build())));

        mockMvc.perform(get("/api/learning/courses?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].courseId").value(3001))
                .andExpect(jsonPath("$.data.list[0].status").value("LEARNING"));
    }
}
