package com.elysia.mooc.ai.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.admin.domain.dto.AiModelConfigQuery;
import com.elysia.mooc.ai.admin.domain.dto.UpdateModelConfigRequest;
import com.elysia.mooc.ai.admin.domain.enums.AiModelScene;
import com.elysia.mooc.ai.admin.domain.vo.AiModelConfigVO;
import com.elysia.mooc.ai.admin.domain.vo.AiUsageVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusCountVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusOverviewVO;
import com.elysia.mooc.ai.admin.service.AiAdminService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** AI 管理后台控制层合同测试。 */
@ExtendWith(MockitoExtension.class)
class AiAdminControllerWebTest {

    @Mock
    private AiAdminService aiAdminService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AiAdminController(aiAdminService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listModelConfigsShouldReturnPageResultAndBindEnums() throws Exception {
        AiModelConfigVO vo = modelConfigVO();
        when(aiAdminService.listModelConfigs(any())).thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/admin/ai/model-configs")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("scene", "CHAT")
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].scene").value("CHAT"))
                .andExpect(jsonPath("$.data.list[0].enabled").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(1))
                .andExpect(jsonPath("$.data.list[0].apiKeyRef").value("DASHSCOPE_API_KEY"))
                .andExpect(jsonPath("$.data.list[0].apiKeyConfigured").value(true));

        ArgumentCaptor<AiModelConfigQuery> captor = ArgumentCaptor.forClass(AiModelConfigQuery.class);
        verify(aiAdminService).listModelConfigs(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getScene()).isEqualTo(AiModelScene.CHAT);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(EnableStatus.ENABLED);
    }

    @Test
    void updateModelConfigShouldReturnSanitizedConfig() throws Exception {
        AiModelConfigVO vo = modelConfigVO();
        vo.setEnabled(EnableStatus.DISABLED);
        vo.setStatus(EnableStatus.DISABLED);
        when(aiAdminService.updateModelConfig(eq(19001L), any())).thenReturn(vo);

        mockMvc.perform(put("/api/admin/ai/model-configs/19001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "provider": "BAILIAN",
                                  "modelName": "qwen-plus",
                                  "baseUrl": "https://dashscope.aliyuncs.com/compatible-mode/v1",
                                  "apiKeyRef": "DASHSCOPE_API_KEY",
                                  "temperature": 0.6,
                                  "topK": 5,
                                  "enabled": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(19001))
                .andExpect(jsonPath("$.data.enabled").value(0))
                .andExpect(jsonPath("$.data.apiKeyRef").value("DASHSCOPE_API_KEY"));

        ArgumentCaptor<UpdateModelConfigRequest> captor = ArgumentCaptor.forClass(UpdateModelConfigRequest.class);
        verify(aiAdminService).updateModelConfig(eq(19001L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getEnabled()).isEqualTo(EnableStatus.DISABLED);
    }

    @Test
    void getDocumentStatusShouldReturnOverview() throws Exception {
        DocumentStatusOverviewVO vo = new DocumentStatusOverviewVO();
        vo.setTotalDocuments(3L);
        vo.setParseSuccess(2L);
        vo.setEmbeddingPending(1L);
        vo.setTotalSegments(6L);
        vo.setParseStatusCounts(List.of(new DocumentStatusCountVO(KnowledgeProcessStatus.SUCCESS, "成功", 2L)));
        vo.setEmbeddingStatusCounts(List.of(new DocumentStatusCountVO(KnowledgeProcessStatus.PENDING, "待处理", 1L)));
        when(aiAdminService.getDocumentStatusOverview()).thenReturn(vo);

        mockMvc.perform(get("/api/admin/ai/documents/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDocuments").value(3))
                .andExpect(jsonPath("$.data.parseSuccess").value(2))
                .andExpect(jsonPath("$.data.embeddingPending").value(1))
                .andExpect(jsonPath("$.data.totalSegments").value(6));
    }

    @Test
    void getUsageShouldReturnAvailableMetrics() throws Exception {
        AiUsageVO vo = new AiUsageVO();
        vo.setMessageCount(4L);
        vo.setTotalTokens(256L);
        vo.setToolCallCount(2L);
        vo.setToolFailedCount(1L);
        vo.setAverageToolCostMs(42.5D);
        vo.setDocumentCount(3L);
        when(aiAdminService.getUsage()).thenReturn(vo);

        mockMvc.perform(get("/api/admin/ai/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messageCount").value(4))
                .andExpect(jsonPath("$.data.totalTokens").value(256))
                .andExpect(jsonPath("$.data.toolCallCount").value(2))
                .andExpect(jsonPath("$.data.toolFailedCount").value(1))
                .andExpect(jsonPath("$.data.averageToolCostMs").value(42.5))
                .andExpect(jsonPath("$.data.documentCount").value(3));
    }

    private AiModelConfigVO modelConfigVO() {
        AiModelConfigVO vo = new AiModelConfigVO();
        vo.setId(19001L);
        vo.setProvider("BAILIAN");
        vo.setModelName("qwen-plus");
        vo.setScene(AiModelScene.CHAT);
        vo.setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
        vo.setApiKeyRef("DASHSCOPE_API_KEY");
        vo.setApiKeyConfigured(true);
        vo.setTemperature(new BigDecimal("0.70"));
        vo.setTopK(5);
        vo.setEnabled(EnableStatus.ENABLED);
        vo.setStatus(EnableStatus.ENABLED);
        vo.setCreateTime(LocalDateTime.of(2026, 5, 16, 19, 0));
        vo.setUpdateTime(LocalDateTime.of(2026, 5, 16, 19, 30));
        return vo;
    }
}
