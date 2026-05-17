package com.elysia.mooc.trade.domain.vo;

import com.elysia.mooc.trade.domain.enums.OrderStatus;
import com.elysia.mooc.trade.domain.enums.PayStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 模拟支付结果。 */
@Data
public class PayResultVO {

    /** 订单 ID。 */
    private Long orderId;

    /** 订单号。 */
    private String orderNo;

    /** 支付流水号。 */
    private String payNo;

    /** 支付状态。 */
    private PayStatus payStatus;

    /** 订单状态。 */
    private OrderStatus orderStatus;

    /** 支付金额。 */
    private BigDecimal payAmount;

    /** 支付时间。 */
    private LocalDateTime payTime;

    /** 是否已发放学习权益。 */
    private Boolean learningGranted;

    /** 中文提示。 */
    private String message;
}
