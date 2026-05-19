package com.elysia.mooc.ops.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.ops.constants.OpsConfigErrorCode;
import com.elysia.mooc.ops.domain.dto.CreateExportJobRequest;
import com.elysia.mooc.ops.domain.dto.CreateReviewTaskRequest;
import com.elysia.mooc.ops.domain.dto.UpdateConfigItemRequest;
import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsConfigValueType;
import com.elysia.mooc.ops.domain.enums.OpsExportJobStatus;
import com.elysia.mooc.ops.domain.enums.OpsReviewPriority;
import com.elysia.mooc.ops.domain.enums.OpsReviewStatus;
import com.elysia.mooc.ops.domain.po.OpsConfigItemPO;
import com.elysia.mooc.ops.domain.po.OpsExportJobPO;
import com.elysia.mooc.ops.domain.po.OpsReviewTaskPO;
import com.elysia.mooc.ops.domain.vo.ConfigItemResultVO;
import com.elysia.mooc.ops.domain.vo.ExportTaskResultVO;
import com.elysia.mooc.ops.domain.vo.ReviewTaskResultVO;
import com.elysia.mooc.ops.mapper.OpsConfigItemMapper;
import com.elysia.mooc.ops.mapper.OpsExportJobMapper;
import com.elysia.mooc.ops.mapper.OpsReviewTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 运营配置服务单元测试。 */
@ExtendWith(MockitoExtension.class)
class OpsConfigServiceImplTest {

    @Mock
    private OpsReviewTaskMapper reviewTaskMapper;

    @Mock
    private OpsExportJobMapper exportJobMapper;

    @Mock
    private OpsConfigItemMapper configItemMapper;

    @Mock
    private UserContextService userContextService;

    private OpsConfigServiceImpl opsConfigService;

    @BeforeEach
    void setUp() {
        opsConfigService = new OpsConfigServiceImpl(
                reviewTaskMapper,
                exportJobMapper,
                configItemMapper,
                userContextService,
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void createReviewTaskShouldPersistPendingTask() {
        when(userContextService.currentUserId()).thenReturn(1L);
        when(reviewTaskMapper.selectCount(any())).thenReturn(0L);
        assignReviewId(29010L);
        CreateReviewTaskRequest request = new CreateReviewTaskRequest();
        request.setTargetType("course");
        request.setTargetId("3003");
        request.setReason("课程提交审核");
        request.setPriority(OpsReviewPriority.HIGH);

        ReviewTaskResultVO result = opsConfigService.createReviewTask(request);

        assertThat(result.getReviewId()).isEqualTo(29010L);
        assertThat(result.getTargetType()).isEqualTo("COURSE");
        assertThat(result.getTargetId()).isEqualTo("3003");
        assertThat(result.getStatus()).isEqualTo(OpsReviewStatus.PENDING);
        assertThat(result.getReason()).isEqualTo("课程提交审核");

        ArgumentCaptor<OpsReviewTaskPO> captor = ArgumentCaptor.forClass(OpsReviewTaskPO.class);
        verify(reviewTaskMapper).insert(captor.capture());
        assertThat(captor.getValue().getSubmitUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getReviewReason()).contains("HIGH", "课程提交审核");
    }

    @Test
    void createReviewTaskShouldRejectPendingDuplicate() {
        when(userContextService.currentUserId()).thenReturn(1L);
        when(reviewTaskMapper.selectCount(any())).thenReturn(1L);
        CreateReviewTaskRequest request = new CreateReviewTaskRequest();
        request.setTargetType("COURSE");
        request.setTargetId("3003");

        assertThatThrownBy(() -> opsConfigService.createReviewTask(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(OpsConfigErrorCode.OPS_REVIEW_PENDING_DUPLICATED.code());
    }

    @Test
    void createExportJobShouldPersistJsonParamsAndReturnPending() throws Exception {
        when(userContextService.currentUserId()).thenReturn(1L);
        assignExportId(29110L);
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setExportType(com.elysia.mooc.ops.domain.enums.OpsExportType.COURSE_AUDIT);
        request.setBizDate(LocalDate.of(2026, 5, 19));

        ExportTaskResultVO result = opsConfigService.createExportJob(request);

        assertThat(result.getExportId()).isEqualTo(29110L);
        assertThat(result.getStatus()).isEqualTo(OpsExportJobStatus.PENDING);
        assertThat(result.getFileName()).isEqualTo("course-audit-2026-05-19.xlsx");

        ArgumentCaptor<OpsExportJobPO> captor = ArgumentCaptor.forClass(OpsExportJobPO.class);
        verify(exportJobMapper).insert(captor.capture());
        JsonNode params = new ObjectMapper().readTree(captor.getValue().getRequestParams());
        assertThat(params.get("exportType").asText()).isEqualTo("COURSE_AUDIT");
        assertThat(params.get("format").asText()).isEqualTo("XLSX");
    }

    @Test
    void updateConfigItemShouldUpdateExistingNumberConfig() {
        OpsConfigItemPO existing = new OpsConfigItemPO();
        existing.setId(29202L);
        existing.setConfigKey("export.downloadExpireHours");
        existing.setConfigGroup("EXPORT");
        existing.setValueType(OpsConfigValueType.NUMBER);
        existing.setStatus(OpsConfigStatus.ENABLED);
        when(configItemMapper.selectOne(any())).thenReturn(existing);
        UpdateConfigItemRequest request = new UpdateConfigItemRequest();
        request.setValue("48");
        request.setRemark("延长导出下载有效期");

        ConfigItemResultVO result = opsConfigService.updateConfigItem("export.downloadExpireHours", request);

        assertThat(result.getConfigKey()).isEqualTo("export.downloadExpireHours");
        assertThat(result.getValue()).isEqualTo("48");
        assertThat(result.getValueType()).isEqualTo(OpsConfigValueType.NUMBER);
        assertThat(result.getStatus()).isEqualTo(OpsConfigStatus.ENABLED);
        verify(configItemMapper).updateById(existing);
    }

    @Test
    void updateConfigItemShouldCreateMissingConfig() {
        when(configItemMapper.selectOne(any())).thenReturn(null);
        assignConfigId(29210L);
        UpdateConfigItemRequest request = new UpdateConfigItemRequest();
        request.setValue("true");

        ConfigItemResultVO result = opsConfigService.updateConfigItem("feature.aiTutorEnabled", request);

        assertThat(result.getConfigKey()).isEqualTo("feature.aiTutorEnabled");
        assertThat(result.getConfigGroup()).isEqualTo("FEATURE");
        assertThat(result.getValueType()).isEqualTo(OpsConfigValueType.BOOLEAN);
        assertThat(result.getStatus()).isEqualTo(OpsConfigStatus.ENABLED);
    }

    @Test
    void updateConfigItemShouldRejectInvalidNumberValue() {
        OpsConfigItemPO existing = new OpsConfigItemPO();
        existing.setId(29202L);
        existing.setConfigKey("export.downloadExpireHours");
        existing.setValueType(OpsConfigValueType.NUMBER);
        when(configItemMapper.selectOne(any())).thenReturn(existing);
        UpdateConfigItemRequest request = new UpdateConfigItemRequest();
        request.setValue("abc");

        assertThatThrownBy(() -> opsConfigService.updateConfigItem("export.downloadExpireHours", request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(OpsConfigErrorCode.OPS_CONFIG_VALUE_INVALID.code());
    }

    private void assignReviewId(Long id) {
        org.mockito.Mockito.doAnswer(invocation -> {
            OpsReviewTaskPO task = invocation.getArgument(0);
            task.setId(id);
            return 1;
        }).when(reviewTaskMapper).insert(any(OpsReviewTaskPO.class));
    }

    private void assignExportId(Long id) {
        org.mockito.Mockito.doAnswer(invocation -> {
            OpsExportJobPO job = invocation.getArgument(0);
            job.setId(id);
            return 1;
        }).when(exportJobMapper).insert(any(OpsExportJobPO.class));
    }

    private void assignConfigId(Long id) {
        AtomicLong idGenerator = new AtomicLong(id);
        org.mockito.Mockito.doAnswer(invocation -> {
            OpsConfigItemPO item = invocation.getArgument(0);
            item.setId(idGenerator.get());
            return 1;
        }).when(configItemMapper).insert(any(OpsConfigItemPO.class));
    }
}
