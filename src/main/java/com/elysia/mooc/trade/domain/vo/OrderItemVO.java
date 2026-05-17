package com.elysia.mooc.trade.domain.vo;

import java.math.BigDecimal;
import lombok.Data;

/** 订单明细响应。 */
@Data
public class OrderItemVO {

    /** 订单明细 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 课程标题。 */
    private String courseTitle;

    /** 课程封面。 */
    private String courseCover;

    /** 单价。 */
    private BigDecimal price;

    /** 数量。 */
    private Integer quantity;
}
