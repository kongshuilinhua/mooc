package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.ops.domain.enums.ProductionCheckStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 阶段一生产化巡检汇总结果。 */
@Data
public class ProductionCheckSummaryVO {

    /** 巡检阶段说明。 */
    private String stage;

    /** 当前巡检时间。 */
    private LocalDateTime checkTime;

    /** 汇总状态。 */
    private ProductionCheckStatus status;

    /** 巡检项总数。 */
    private Integer totalCount;

    /** 通过数量。 */
    private Integer passCount;

    /** 警告数量。 */
    private Integer warnCount;

    /** 失败数量。 */
    private Integer failedCount;

    /** 中文汇总说明。 */
    private String message;

    /** 巡检明细。 */
    private List<ProductionCheckItemVO> items;
}
