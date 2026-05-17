package com.elysia.mooc.trade.domain.vo;

import com.elysia.mooc.trade.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 订单响应。 */
@Data
public class OrderVO {

    /** 订单 ID。 */
    private Long id;

    /** 订单号。 */
    private String orderNo;

    /** 用户 ID。 */
    private Long userId;

    /** 订单总金额。 */
    private BigDecimal totalAmount;

    /** 实付金额。 */
    private BigDecimal payAmount;

    /** 订单状态。 */
    private OrderStatus status;

    /** 订单状态中文说明。 */
    private String statusDesc;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 支付时间。 */
    private LocalDateTime payTime;

    /** 取消时间。 */
    private LocalDateTime cancelTime;

    /** 订单明细。 */
    private List<OrderItemVO> items;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
