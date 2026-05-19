package com.elysia.mooc.ops.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.audit.AuditLog;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.ops.domain.dto.CreateExportJobRequest;
import com.elysia.mooc.ops.domain.dto.CreateReviewTaskRequest;
import com.elysia.mooc.ops.domain.dto.UpdateConfigItemRequest;
import com.elysia.mooc.ops.domain.vo.ConfigItemResultVO;
import com.elysia.mooc.ops.domain.vo.ExportTaskResultVO;
import com.elysia.mooc.ops.domain.vo.ReviewTaskResultVO;
import com.elysia.mooc.ops.service.OpsConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端审核导出与运营配置接口。 */
@Tag(name = "管理端审核导出与运营配置")
@Validated
@RestController
@RequestMapping("/api/admin/ops-config")
@RequiredArgsConstructor
public class OpsConfigController {

    private final OpsConfigService opsConfigService;

    /**
     * 创建审核任务。
     *
     * @param request 审核任务请求
     * @return 审核任务创建结果
     */
    @Operation(summary = "创建审核任务")
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @ParamChecker
    @AuditLog(action = "OPS_REVIEW_CREATE", targetType = "OPS_REVIEW_TASK", targetId = "#request.targetId")
    public ApiResult<ReviewTaskResultVO> createReviewTask(@Valid @RequestBody CreateReviewTaskRequest request) {
        return ApiResult.ok(opsConfigService.createReviewTask(request));
    }

    /**
     * 创建导出任务。
     *
     * @param request 导出任务请求
     * @return 导出任务创建结果
     */
    @Operation(summary = "创建导出任务")
    @PostMapping("/exports")
    @PreAuthorize("hasRole('ADMIN')")
    @ParamChecker
    @AuditLog(action = "OPS_EXPORT_CREATE", targetType = "OPS_EXPORT_JOB", targetId = "#request.exportType")
    public ApiResult<ExportTaskResultVO> createExportJob(@Valid @RequestBody CreateExportJobRequest request) {
        return ApiResult.ok(opsConfigService.createExportJob(request));
    }

    /**
     * 更新系统配置项。
     *
     * @param configKey 配置键
     * @param request 配置更新请求
     * @return 最新配置项
     */
    @Operation(summary = "更新系统配置项")
    @PutMapping("/items/{configKey}")
    @PreAuthorize("hasRole('ADMIN')")
    @ParamChecker
    @AuditLog(action = "OPS_CONFIG_UPDATE", targetType = "OPS_CONFIG_ITEM", targetId = "#configKey")
    public ApiResult<ConfigItemResultVO> updateConfigItem(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateConfigItemRequest request) {
        return ApiResult.ok(opsConfigService.updateConfigItem(configKey, request));
    }
}
