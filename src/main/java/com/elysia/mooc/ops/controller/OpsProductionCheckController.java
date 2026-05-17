package com.elysia.mooc.ops.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.ops.domain.vo.ProductionCheckSummaryVO;
import com.elysia.mooc.ops.service.ProductionCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端阶段一生产化巡检接口。 */
@Tag(name = "管理端生产化巡检")
@RestController
@RequestMapping("/api/admin/ops")
@RequiredArgsConstructor
public class OpsProductionCheckController {

    private final ProductionCheckService productionCheckService;

    /**
     * 查询 day01-day23 阶段一生产化巡检结果。
     * @return 阶段一巡检汇总
     */
    @Operation(summary = "查询阶段一生产化巡检结果")
    @GetMapping("/stage-one-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<ProductionCheckSummaryVO> checkStageOne() {
        return ApiResult.ok(productionCheckService.checkStageOne());
    }
}
