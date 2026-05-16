package com.elysia.mooc.ai.admin.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.admin.constants.AiAdminErrorCode;
import com.elysia.mooc.ai.admin.domain.dto.AiModelConfigQuery;
import com.elysia.mooc.ai.admin.domain.dto.UpdateModelConfigRequest;
import com.elysia.mooc.ai.admin.domain.enums.AiModelScene;
import com.elysia.mooc.ai.admin.domain.po.AiModelConfigPO;
import com.elysia.mooc.ai.admin.domain.vo.AiModelConfigVO;
import com.elysia.mooc.ai.admin.domain.vo.AiUsageVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusOverviewVO;
import com.elysia.mooc.ai.admin.mapper.AiModelConfigMapper;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import com.elysia.mooc.ai.chat.mapper.AiMessageMapper;
import com.elysia.mooc.ai.tool.domain.po.AiToolCallLogPO;
import com.elysia.mooc.ai.tool.mapper.AiToolCallLogMapper;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** AI 管理后台服务测试。 */
@ExtendWith(MockitoExtension.class)
class AiAdminServiceImplTest {

    @Mock
    private AiModelConfigMapper aiModelConfigMapper;

    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Mock
    private AiMessageMapper aiMessageMapper;

    @Mock
    private AiToolCallLogMapper aiToolCallLogMapper;

    private AiAdminServiceImpl aiAdminService;

    @BeforeEach
    void setUp() {
        aiAdminService = new AiAdminServiceImpl(
                aiModelConfigMapper,
                knowledgeDocumentMapper,
                aiMessageMapper,
                aiToolCallLogMapper);
    }

    @Test
    void listModelConfigsShouldUsePageResultAndSanitizeApiKey() {
        Page<AiModelConfigPO> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(modelConfig()));
        when(aiModelConfigMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        AiModelConfigQuery query = new AiModelConfigQuery();
        query.setScene(AiModelScene.CHAT);
        query.setStatus(EnableStatus.ENABLED);
        PageResult<AiModelConfigVO> result = aiAdminService.listModelConfigs(query);

        org.assertj.core.api.Assertions.assertThat(result.getTotal()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(result.getList().get(0).getApiKeyRef())
                .isEqualTo("DASHSCOPE_API_KEY");
        org.assertj.core.api.Assertions.assertThat(result.getList().get(0).getApiKeyConfigured()).isTrue();
        org.assertj.core.api.Assertions.assertThat(result.getList().get(0).getStatus())
                .isEqualTo(EnableStatus.ENABLED);
    }

    @Test
    void updateModelConfigShouldChangeEditableFields() {
        when(aiModelConfigMapper.selectById(19001L))
                .thenReturn(modelConfig())
                .thenReturn(modelConfig());
        UpdateModelConfigRequest request = new UpdateModelConfigRequest();
        request.setProvider("bailian");
        request.setModelName("qwen-max");
        request.setApiKeyRef("DASHSCOPE_API_KEY");
        request.setTemperature(new BigDecimal("0.60"));
        request.setTopK(8);
        request.setEnabled(EnableStatus.DISABLED);

        aiAdminService.updateModelConfig(19001L, request);

        ArgumentCaptor<AiModelConfigPO> captor = ArgumentCaptor.forClass(AiModelConfigPO.class);
        verify(aiModelConfigMapper).updateById(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getProvider()).isEqualTo("BAILIAN");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getModelName()).isEqualTo("qwen-max");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getEnabled()).isEqualTo(EnableStatus.DISABLED);
    }

    @Test
    void updateModelConfigShouldRejectRawApiKey() {
        when(aiModelConfigMapper.selectById(19001L)).thenReturn(modelConfig());
        UpdateModelConfigRequest request = new UpdateModelConfigRequest();
        request.setApiKeyRef("sk-raw-secret");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> aiAdminService.updateModelConfig(19001L, request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(AiAdminErrorCode.AI_ADMIN_PARAM_INVALID.code());
    }

    @Test
    void getDocumentStatusOverviewShouldAggregateStatuses() {
        when(knowledgeDocumentMapper.selectMaps(any(Wrapper.class)))
                .thenReturn(List.of(
                        Map.of("status", "SUCCESS", "total", 2L),
                        Map.of("status", "PENDING", "total", 1L),
                        Map.of("status", "PARSING", "total", 1L)))
                .thenReturn(List.of(
                        Map.of("status", "PENDING", "total", 2L),
                        Map.of("status", "FAILED", "total", 1L)));
        when(knowledgeDocumentMapper.selectObjs(any(Wrapper.class))).thenReturn(List.of(6L));
        when(knowledgeDocumentMapper.selectCount(any(Wrapper.class))).thenReturn(3L);

        DocumentStatusOverviewVO result = aiAdminService.getDocumentStatusOverview();

        org.assertj.core.api.Assertions.assertThat(result.getTotalDocuments()).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(result.getParseSuccess()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(result.getParseProcessing()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(result.getEmbeddingFailed()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(result.getTotalSegments()).isEqualTo(6);
    }

    @Test
    void getUsageShouldReturnAvailableMetrics() {
        when(aiMessageMapper.selectCount(any(Wrapper.class))).thenReturn(4L, 2L, 2L, 3L, 1L);
        when(aiMessageMapper.selectObjs(any(Wrapper.class))).thenReturn(List.of(100L), List.of(50L), List.of(150L));
        when(aiToolCallLogMapper.selectCount(any(Wrapper.class))).thenReturn(3L, 2L, 1L);
        when(aiToolCallLogMapper.selectObjs(any(Wrapper.class))).thenReturn(List.of(new BigDecimal("40.50")));
        when(knowledgeDocumentMapper.selectCount(any(Wrapper.class))).thenReturn(5L, 4L, 3L);

        AiUsageVO result = aiAdminService.getUsage();

        org.assertj.core.api.Assertions.assertThat(result.getMessageCount()).isEqualTo(4);
        org.assertj.core.api.Assertions.assertThat(result.getTotalTokens()).isEqualTo(150);
        org.assertj.core.api.Assertions.assertThat(result.getToolCallCount()).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(result.getAverageToolCostMs()).isEqualTo(40.5D);
        org.assertj.core.api.Assertions.assertThat(result.getEmbeddedDocumentCount()).isEqualTo(3);
    }

    private AiModelConfigPO modelConfig() {
        AiModelConfigPO po = new AiModelConfigPO();
        po.setId(19001L);
        po.setProvider("BAILIAN");
        po.setModelName("qwen-plus");
        po.setScene(AiModelScene.CHAT);
        po.setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
        po.setApiKeyRef("DASHSCOPE_API_KEY");
        po.setTemperature(new BigDecimal("0.70"));
        po.setTopK(5);
        po.setEnabled(EnableStatus.ENABLED);
        po.setCreateTime(LocalDateTime.of(2026, 5, 16, 19, 0));
        po.setUpdateTime(LocalDateTime.of(2026, 5, 16, 19, 30));
        return po;
    }
}
