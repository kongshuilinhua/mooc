package com.elysia.mooc.trade.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 取消订单请求。 */
@Data
public class CancelOrderRequest {

    /** 取消原因。 */
    @Size(max = 200, message = "取消原因不能超过200个字符")
    private String reason;
}
