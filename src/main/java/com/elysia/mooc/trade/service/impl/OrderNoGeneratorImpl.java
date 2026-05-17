package com.elysia.mooc.trade.service.impl;

import com.elysia.mooc.trade.constants.TradeConstants;
import com.elysia.mooc.trade.service.OrderNoGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** 默认订单号生成器。 */
@Component
public class OrderNoGeneratorImpl implements OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int SEQUENCE_LIMIT = 10_000;
    private final AtomicInteger sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt(SEQUENCE_LIMIT));

    /**
     * 生成订单号。
     *
     * @return 全局唯一订单号
     */
    @Override
    public String nextOrderNo() {
        return next(TradeConstants.ORDER_NO_PREFIX);
    }

    /**
     * 生成支付流水号。
     *
     * @return 全局唯一支付流水号
     */
    @Override
    public String nextPayNo() {
        return next(TradeConstants.PAY_NO_PREFIX);
    }

    private String next(String prefix) {
        int next = sequence.updateAndGet(value -> value >= SEQUENCE_LIMIT - 1 ? 0 : value + 1);
        return prefix + LocalDateTime.now().format(FORMATTER) + String.format("%04d", next);
    }
}
