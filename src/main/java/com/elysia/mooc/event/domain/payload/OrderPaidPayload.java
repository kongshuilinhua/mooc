package com.elysia.mooc.event.domain.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单支付成功事件载荷。 */
public record OrderPaidPayload(
        Long orderId,
        String orderNo,
        Long userId,
        Long courseId,
        BigDecimal payAmount,
        String payNo,
        LocalDateTime payTime) {
}
