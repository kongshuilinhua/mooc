package com.elysia.mooc.trade.domain.dto;

import com.elysia.mooc.common.validate.EnumValid;
import com.elysia.mooc.trade.domain.enums.PayChannel;
import lombok.Data;

/** 模拟支付请求。 */
@Data
public class MockPayRequest {

    /** 支付渠道，默认 MOCK。 */
    @EnumValid(enumClass = PayChannel.class, message = "支付渠道不合法")
    private String payChannel;
}
