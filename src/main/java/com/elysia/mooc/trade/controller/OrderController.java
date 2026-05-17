package com.elysia.mooc.trade.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.trade.domain.dto.CancelOrderRequest;
import com.elysia.mooc.trade.domain.dto.CreateOrderRequest;
import com.elysia.mooc.trade.domain.dto.OrderQuery;
import com.elysia.mooc.trade.domain.vo.OrderVO;
import com.elysia.mooc.trade.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 订单接口。 */
@Tag(name = "订单与支付")
@Validated
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单。
     *
     * @param request 创建订单请求
     * @return 订单详情
     */
    @Operation(summary = "创建订单")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<OrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResult.ok(orderService.createOrder(request));
    }

    /**
     * 分页查询我的订单。
     *
     * @param query 查询参数
     * @return 订单分页
     */
    @Operation(summary = "我的订单")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<OrderVO>> listMyOrders(@Valid OrderQuery query) {
        return ApiResult.ok(orderService.listMyOrders(query));
    }

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<OrderVO> getOrderDetail(@PathVariable Long orderId) {
        return ApiResult.ok(orderService.getOrderDetail(orderId));
    }

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @param request 取消请求
     * @return 取消后的订单
     */
    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<OrderVO> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {
        return ApiResult.ok(orderService.cancelOrder(orderId, request));
    }
}
