package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.ops.domain.enums.ProductionCheckStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 阶段一生产化巡检单项结果。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionCheckItemVO {

    /** 巡检项编码。 */
    private String code;

    /** 巡检项名称。 */
    private String name;

    /** 所属分组。 */
    private String groupName;

    /** 当前值，通常为聚合数量或配置状态。 */
    private Long currentValue;

    /** 期望最小值，非数量型巡检项可为空。 */
    private Long expectedValue;

    /** 巡检状态。 */
    private ProductionCheckStatus status;

    /** 中文说明。 */
    private String message;
}
