package com.elysia.mooc.statistics.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.statistics.domain.dto.DailyStatsQuery;
import com.elysia.mooc.statistics.domain.vo.AdminOverviewVO;
import com.elysia.mooc.statistics.domain.vo.DailyStatsVO;

/** 管理端数据统计服务。 */
public interface AdminStatisticsService {

    /**
     * 查询管理端今日概览，统计表缺失时从基础表做轻量兜底。
     *
     * @return 管理端概览
     */
    AdminOverviewVO getOverview();

    /**
     * 分页查询每日统计。
     *
     * @param query 查询参数
     * @return 每日统计分页
     */
    PageResult<DailyStatsVO> listDailyStats(DailyStatsQuery query);
}
