package com.elysia.mooc.ops.service;

import com.elysia.mooc.ops.domain.vo.ProductionCheckSummaryVO;

/** 阶段一生产化巡检服务。 */
public interface ProductionCheckService {

    /**
     * 执行 day01-day23 阶段一生产化巡检。
     * @return 巡检汇总结果
     */
    ProductionCheckSummaryVO checkStageOne();
}
