package com.elysia.mooc.ops.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.ops.domain.dto.CreateExportJobRequest;
import com.elysia.mooc.ops.domain.dto.CreateReviewTaskRequest;
import com.elysia.mooc.ops.domain.dto.UpdateConfigItemRequest;
import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsExportJobStatus;
import com.elysia.mooc.ops.domain.enums.OpsExportType;
import com.elysia.mooc.ops.domain.enums.OpsReviewStatus;
import com.elysia.mooc.ops.domain.vo.ConfigItemResultVO;
import com.elysia.mooc.ops.domain.vo.ExportTaskResultVO;
import com.elysia.mooc.ops.domain.vo.ReviewTaskResultVO;
import com.elysia.mooc.ops.service.OpsConfigService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 运营配置接口合同测试。 */
@ExtendWith(MockitoExtension.class)
class OpsConfigControllerWebTest {

    @Mock
    private OpsConfigService opsConfigService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new OpsConfigController(opsConfigService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReviewTaskShouldReturnFrontendFields() throws Exception {
        ReviewTaskResultVO result = new ReviewTaskResultVO();
        result.setReviewId(29010L);
        result.setTargetType("COURSE");
        result.setTargetId("3003");
        result.setStatus(OpsReviewStatus.PENDING);
        result.setReason("课程提交审核");
        result.setCreatedAt(LocalDateTime.of(2026, 5, 19, 16, 0));
        when(opsConfigService.createReviewTask(any())).thenReturn(result);

        mockMvc.perform(post("/api/admin/ops-config/reviews")
                        .contentType("application/json")
                        .content("""
                                {"targetType":"COURSE","targetId":"3003","reason":"课程提交审核","priority":"MEDIUM"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(29010))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.reason").value("课程提交审核"));

        ArgumentCaptor<CreateReviewTaskRequest> captor = ArgumentCaptor.forClass(CreateReviewTaskRequest.class);
        verify(opsConfigService).createReviewTask(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getTargetType()).isEqualTo("COURSE");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getTargetId()).isEqualTo("3003");
    }

    @Test
    void createExportJobShouldReturnPendingTask() throws Exception {
        ExportTaskResultVO result = new ExportTaskResultVO();
        result.setExportId(29110L);
        result.setExportType(OpsExportType.COURSE_AUDIT);
        result.setStatus(OpsExportJobStatus.PENDING);
        result.setFileName("course-audit-2026-05-19.xlsx");
        result.setCreatedAt(LocalDateTime.of(2026, 5, 19, 16, 0));
        when(opsConfigService.createExportJob(any())).thenReturn(result);

        mockMvc.perform(post("/api/admin/ops-config/exports")
                        .contentType("application/json")
                        .content("""
                                {"exportType":"COURSE_AUDIT","bizDate":"2026-05-19","format":"XLSX"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exportId").value(29110))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.fileName").value("course-audit-2026-05-19.xlsx"));

        ArgumentCaptor<CreateExportJobRequest> captor = ArgumentCaptor.forClass(CreateExportJobRequest.class);
        verify(opsConfigService).createExportJob(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getExportType()).isEqualTo(OpsExportType.COURSE_AUDIT);
    }

    @Test
    void updateConfigItemShouldReturnUpdatedAt() throws Exception {
        ConfigItemResultVO result = new ConfigItemResultVO();
        result.setConfigKey("export.downloadExpireHours");
        result.setValue("24");
        result.setStatus(OpsConfigStatus.ENABLED);
        result.setUpdatedAt(LocalDateTime.of(2026, 5, 19, 16, 0));
        when(opsConfigService.updateConfigItem(eq("export.downloadExpireHours"), any())).thenReturn(result);

        mockMvc.perform(put("/api/admin/ops-config/items/export.downloadExpireHours")
                        .contentType("application/json")
                        .content("""
                                {"value":"24","remark":"day29联调"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configKey").value("export.downloadExpireHours"))
                .andExpect(jsonPath("$.data.value").value("24"))
                .andExpect(jsonPath("$.data.updatedAt").exists());

        ArgumentCaptor<UpdateConfigItemRequest> captor = ArgumentCaptor.forClass(UpdateConfigItemRequest.class);
        verify(opsConfigService).updateConfigItem(eq("export.downloadExpireHours"), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getValue()).isEqualTo("24");
    }

    @Test
    void createReviewTaskShouldRejectInvalidTargetId() throws Exception {
        when(opsConfigService.createReviewTask(any()))
                .thenThrow(new BizException(CommonErrorCode.PARAM_INVALID, "目标ID必须为正数"));

        mockMvc.perform(post("/api/admin/ops-config/reviews")
                        .contentType("application/json")
                        .content("""
                                {"targetType":"COURSE","targetId":"abc","reason":"参数错误"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("目标ID必须为正数"));
    }
}
