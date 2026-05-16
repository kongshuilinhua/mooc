package com.elysia.mooc.ai.admin.controller;

import com.elysia.mooc.ai.admin.domain.dto.AiModelConfigQuery;
import com.elysia.mooc.ai.admin.domain.dto.UpdateModelConfigRequest;
import com.elysia.mooc.ai.admin.domain.vo.AiModelConfigVO;
import com.elysia.mooc.ai.admin.domain.vo.AiUsageVO;
import com.elysia.mooc.ai.admin.domain.vo.DocumentStatusOverviewVO;
import com.elysia.mooc.ai.admin.service.AiAdminService;
import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 管理后台接口。 */
@Tag(name = "AI 管理后台")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai")
public class AiAdminController {

    private final AiAdminService aiAdminService;

    /**
     * 分页查询模型配置。
     *
     * @param query 查询条件
     * @return 模型配置分页
     */
    @Operation(summary = "分页查询模型配置")
    @GetMapping("/model-configs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<AiModelConfigVO>> listModelConfigs(@Valid AiModelConfigQuery query) {
        return ApiResult.ok(aiAdminService.listModelConfigs(query));
    }

    /**
     * 修改模型配置。
     *
     * @param id 模型配置 ID
     * @param request 修改请求
     * @return 修改后的模型配置
     */
    @Operation(summary = "修改模型配置")
    @PutMapping("/model-configs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<AiModelConfigVO> updateModelConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModelConfigRequest request) {
        return ApiResult.ok(aiAdminService.updateModelConfig(id, request));
    }

    /**
     * 查询知识库文档处理状态统计。
     *
     * @return 文档处理状态统计
     */
    @Operation(summary = "查询知识库文档处理状态统计")
    @GetMapping("/documents/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<DocumentStatusOverviewVO> getDocumentStatusOverview() {
        return ApiResult.ok(aiAdminService.getDocumentStatusOverview());
    }

    /**
     * 查询 AI 调用统计。
     *
     * @return AI 调用统计
     */
    @Operation(summary = "查询 AI 调用统计")
    @GetMapping("/usage")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<AiUsageVO> getUsage() {
        return ApiResult.ok(aiAdminService.getUsage());
    }
}
