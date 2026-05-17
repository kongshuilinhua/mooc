package com.elysia.mooc.recommend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.course.domain.enums.CoursePriceType;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.recommend.domain.dto.CourseRecommendQuery;
import com.elysia.mooc.recommend.domain.vo.HotConceptVO;
import com.elysia.mooc.recommend.domain.vo.HotCourseVO;
import com.elysia.mooc.recommend.domain.vo.RecommendedCourseVO;
import com.elysia.mooc.recommend.service.RecommendService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 推荐接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class RecommendControllerWebTest {

    @Mock
    private RecommendService recommendService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RecommendController(recommendService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recommendationsShouldReturnPageResult() throws Exception {
        RecommendedCourseVO vo = new RecommendedCourseVO();
        vo.setId(3001L);
        vo.setTitle("Spring Boot 3 实战入门");
        vo.setStatus(CourseStatus.PUBLISHED);
        vo.setPriceType(CoursePriceType.FREE);
        vo.setHotScore(new BigDecimal("93.50"));
        vo.setReason("你最近在学习 Spring Boot");
        when(recommendService.listRecommendations(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/courses/recommendations")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(3001))
                .andExpect(jsonPath("$.data.list[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.list[0].reason").value("你最近在学习 Spring Boot"));

        ArgumentCaptor<CourseRecommendQuery> captor = ArgumentCaptor.forClass(CourseRecommendQuery.class);
        verify(recommendService).listRecommendations(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getKeyword()).isEqualTo("Spring");
    }

    @Test
    void hotCoursesShouldReturnHotFields() throws Exception {
        HotCourseVO vo = new HotCourseVO();
        vo.setId(3002L);
        vo.setTitle("Vue3 项目实战");
        vo.setViewCount(90);
        vo.setLearnCount(18);
        vo.setFavoriteCount(3);
        vo.setRatingScore(new BigDecimal("4.60"));
        vo.setHotScore(new BigDecimal("76.80"));
        when(recommendService.listHotCourses(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/courses/hot?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].viewCount").value(90))
                .andExpect(jsonPath("$.data.list[0].hotScore").value(76.80));
    }

    @Test
    void hotConceptsShouldReturnConceptList() throws Exception {
        HotConceptVO vo = new HotConceptVO();
        vo.setConceptId(5101L);
        vo.setConceptName("RAG 检索增强");
        vo.setCourseId(3003L);
        vo.setCourseTitle("AI 与 RAG 实战");
        vo.setScore(new BigDecimal("88.00"));
        vo.setHitCount(88);
        when(recommendService.listHotConcepts(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/concepts/hot?pageNo=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].conceptId").value(5101))
                .andExpect(jsonPath("$.data.list[0].courseTitle").value("AI 与 RAG 实战"));
    }
}
