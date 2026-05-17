package com.elysia.mooc.statistics.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 后台每日统计查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DailyStatsQuery extends PageQuery {

    /** 起始统计日期。 */
    private LocalDate startDate;

    /** 结束统计日期。 */
    private LocalDate endDate;
}
