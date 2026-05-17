package com.elysia.mooc.trade.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.trade.domain.dto.CancelOrderRequest;
import com.elysia.mooc.trade.domain.dto.CreateOrderRequest;
import com.elysia.mooc.trade.domain.dto.OrderQuery;
import com.elysia.mooc.trade.domain.vo.OrderVO;

/** 订单服务。 */
public interface OrderService {

    /**
     * 创建订单。
     *
     * @param request 创建订单请求
     * @return 订单详情
     */
    OrderVO createOrder(CreateOrderRequest request);

    /**
     * 分页查询我的订单。
     *
     * @param query 查询参数
     * @return 订单分页
     */
    PageResult<OrderVO> listMyOrders(OrderQuery query);

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    OrderVO getOrderDetail(Long orderId);

    /**
     * 取消订单。
     *
     * @param orderId 订单 ID
     * @param request 取消请求
     * @return 取消后的订单
     */
    OrderVO cancelOrder(Long orderId, CancelOrderRequest request);
}
