package com.elysia.mooc.trade.constants;

/** 交易模块常量。 */
public final class TradeConstants {

    /** 订单号前缀。 */
    public static final String ORDER_NO_PREFIX = "ORD";

    /** 支付流水号前缀。 */
    public static final String PAY_NO_PREFIX = "PAY";

    /** 默认订单过期分钟数。 */
    public static final long DEFAULT_ORDER_EXPIRE_MINUTES = 30L;

    /** 订单支付成功后的学习权益消费组。 */
    public static final String ORDER_PAID_LEARNING_CONSUMER_GROUP = "trade-learning-grant";

    /** 默认模拟支付渠道。 */
    public static final String DEFAULT_MOCK_PAY_CHANNEL = "MOCK";

    private TradeConstants() {
    }
}
