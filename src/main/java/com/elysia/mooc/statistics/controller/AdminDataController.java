package com.elysia.mooc.statistics.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.statistics.domain.dto.DailyStatsQuery;
import com.elysia.mooc.statistics.domain.vo.AdminOverviewVO;
import com.elysia.mooc.statistics.domain.vo.DailyStatsVO;
import com.elysia.mooc.statistics.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端数据统计接口。 */
@Tag(name = "管理端数据统计")
@Validated
@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
public class AdminDataController {

    private final AdminStatisticsService adminStatisticsService;

    /**
     * 查询后台数据概览。
     *
     * @return 后台数据概览
     */
    @Operation(summary = "查询后台数据概览")
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<AdminOverviewVO> getOverview() {
        return ApiResult.ok(adminStatisticsService.getOverview());
    }

    /**
     * 查询每日统计。
     *
     * @param query 查询参数
     * @return 每日统计分页
     */
    @Operation(summary = "查询每日统计")
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<DailyStatsVO>> listDailyStats(@Valid DailyStatsQuery query) {
        return ApiResult.ok(adminStatisticsService.listDailyStats(query));
    }
}
