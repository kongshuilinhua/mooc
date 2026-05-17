package com.elysia.mooc.trade.service;

/** 订单号与支付流水号生成器。 */
public interface OrderNoGenerator {

    /**
     * 生成订单号。
     *
     * @return 全局唯一订单号
     */
    String nextOrderNo();

    /**
     * 生成支付流水号。
     *
     * @return 全局唯一支付流水号
     */
    String nextPayNo();
}
