package com.elysia.mooc.ops.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.ops.domain.enums.ProductionCheckStatus;
import com.elysia.mooc.ops.domain.vo.ProductionCheckItemVO;
import com.elysia.mooc.ops.domain.vo.ProductionCheckSummaryVO;
import com.elysia.mooc.ops.service.ProductionCheckService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 阶段一生产化巡检接口 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class OpsProductionCheckControllerWebTest {

    @Mock
    private ProductionCheckService productionCheckService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OpsProductionCheckController(productionCheckService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void checkStageOneShouldReturnSummaryAndItems() throws Exception {
        ProductionCheckSummaryVO summary = new ProductionCheckSummaryVO();
        summary.setStage("day01-day23 阶段一");
        summary.setCheckTime(LocalDateTime.of(2026, 5, 17, 15, 0));
        summary.setStatus(ProductionCheckStatus.WARN);
        summary.setTotalCount(2);
        summary.setPassCount(1);
        summary.setWarnCount(1);
        summary.setFailedCount(0);
        summary.setMessage("阶段一核心链路可巡检，部分演示数据或外部依赖数据不足");
        summary.setItems(List.of(
                new ProductionCheckItemVO("USER_COUNT", "用户数据", "演示数据", 3L, 3L,
                        ProductionCheckStatus.PASS, "已满足阶段一联调要求"),
                new ProductionCheckItemVO("RAG_CONVERSATION_COUNT", "RAG 会话", "AI 能力", 0L, 1L,
                        ProductionCheckStatus.WARN, "暂未发现数据，不阻塞启动，但会影响完整演示")));
        when(productionCheckService.checkStageOne()).thenReturn(summary);

        mockMvc.perform(get("/api/admin/ops/stage-one-check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.stage").value("day01-day23 阶段一"))
                .andExpect(jsonPath("$.data.status").value("WARN"))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.items[0].code").value("USER_COUNT"))
                .andExpect(jsonPath("$.data.items[1].currentValue").value(0))
                .andExpect(jsonPath("$.data.items[1].status").value("WARN"));
    }
}
