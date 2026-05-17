package com.elysia.mooc.statistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.statistics.domain.dto.DailyStatsQuery;
import com.elysia.mooc.statistics.domain.vo.AdminOverviewVO;
import com.elysia.mooc.statistics.domain.vo.DailyStatsVO;
import com.elysia.mooc.statistics.service.AdminStatisticsService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 管理端数据统计接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class AdminDataControllerWebTest {

    @Mock
    private AdminStatisticsService adminStatisticsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminDataController(adminStatisticsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void overviewShouldReturnAdminMetrics() throws Exception {
        AdminOverviewVO vo = new AdminOverviewVO();
        vo.setUserCount(8L);
        vo.setNewUserCount(3);
        vo.setActiveUserCount(6);
        vo.setCourseCount(12L);
        vo.setCourseViewCount(280);
        vo.setVideoPlayCount(96);
        vo.setLearnSeconds(12600L);
        vo.setLearningMinutes(210L);
        vo.setAiRequestCount(14);
        when(adminStatisticsService.getOverview()).thenReturn(vo);

        mockMvc.perform(get("/api/admin/data/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCount").value(8))
                .andExpect(jsonPath("$.data.learningMinutes").value(210))
                .andExpect(jsonPath("$.data.aiRequestCount").value(14));
    }

    @Test
    void dailyShouldReturnPageResultAndBindDateRange() throws Exception {
        DailyStatsVO vo = new DailyStatsVO();
        vo.setStatDate(LocalDate.of(2026, 5, 17));
        vo.setNewUserCount(3);
        vo.setLearnSeconds(12600L);
        vo.setLearningMinutes(210L);
        vo.setAiCallCount(14);
        when(adminStatisticsService.listDailyStats(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/admin/data/daily")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("startDate", "2026-05-01")
                        .param("endDate", "2026-05-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].statDate").value("2026-05-17"))
                .andExpect(jsonPath("$.data.list[0].learningMinutes").value(210));

        ArgumentCaptor<DailyStatsQuery> captor = ArgumentCaptor.forClass(DailyStatsQuery.class);
        verify(adminStatisticsService).listDailyStats(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStartDate())
                .isEqualTo(LocalDate.of(2026, 5, 1));
    }
}
