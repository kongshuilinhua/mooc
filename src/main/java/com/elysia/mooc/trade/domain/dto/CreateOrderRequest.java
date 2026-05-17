package com.elysia.mooc.trade.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 创建订单请求。 */
@Data
public class CreateOrderRequest {

    /** 购买课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    private Long courseId;

    /** 优惠券 ID，day21 仅预留，不参与优惠计算。 */
    @Positive(message = "优惠券ID必须为正数")
    private Long couponId;
}
