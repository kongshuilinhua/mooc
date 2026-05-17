package com.elysia.mooc.trade.service;

import com.elysia.mooc.trade.domain.dto.MockPayRequest;
import com.elysia.mooc.trade.domain.vo.PayResultVO;

/** 支付服务。 */
public interface PayService {

    /**
     * 模拟支付订单。
     *
     * @param orderId 订单 ID
     * @param request 模拟支付请求
     * @return 支付结果
     */
    PayResultVO mockPay(Long orderId, MockPayRequest request);
}
