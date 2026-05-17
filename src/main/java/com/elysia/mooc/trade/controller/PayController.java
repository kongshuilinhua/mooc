package com.elysia.mooc.trade.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.trade.domain.dto.MockPayRequest;
import com.elysia.mooc.trade.domain.vo.PayResultVO;
import com.elysia.mooc.trade.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 支付接口。 */
@Tag(name = "订单与支付")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 模拟支付。
     *
     * @param orderId 订单 ID
     * @param request 模拟支付请求
     * @return 支付结果
     */
    @Operation(summary = "模拟支付")
    @PostMapping("/{orderId}/pay/mock")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PayResultVO> mockPay(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) MockPayRequest request) {
        return ApiResult.ok(payService.mockPay(orderId, request));
    }
}
