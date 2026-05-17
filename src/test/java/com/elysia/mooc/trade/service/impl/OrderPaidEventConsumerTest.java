package com.elysia.mooc.trade.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.OrderPaidPayload;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.elysia.mooc.learning.service.LearningService;
import com.elysia.mooc.trade.constants.TradeConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 订单支付事件消费者测试。 */
@ExtendWith(MockitoExtension.class)
class OrderPaidEventConsumerTest {

    @Mock
    private ConsumerIdempotentService consumerIdempotentService;

    @Mock
    private LearningService learningService;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private OrderPaidEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderPaidEventConsumer(consumerIdempotentService, learningService, objectMapper);
    }

    @Test
    void consumeOrderPaidShouldGrantPurchasedCourseOnce() {
        doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return true;
        }).when(consumerIdempotentService).executeOnce(
                any(), eq(TradeConstants.ORDER_PAID_LEARNING_CONSUMER_GROUP), any());
        DomainEvent event = event();

        boolean executed = consumer.consumeOrderPaid(event);

        assertThat(executed).isTrue();
        verify(learningService).grantPurchasedCourse(3L, 3002L);
    }

    @Test
    void consumeOrderPaidShouldSkipDuplicateEvent() {
        when(consumerIdempotentService.executeOnce(
                any(), eq(TradeConstants.ORDER_PAID_LEARNING_CONSUMER_GROUP), any())).thenReturn(false);

        boolean executed = consumer.consumeOrderPaid(event());

        assertThat(executed).isFalse();
    }

    private DomainEvent event() {
        return DomainEvent.of(
                EventTopicConstants.TRADE_ORDER_PAID,
                EventTopicConstants.TRADE_ORDER_PAID,
                "order:ORD1",
                new OrderPaidPayload(
                        21001L,
                        "ORD1",
                        3L,
                        3002L,
                        new BigDecimal("199.00"),
                        "PAY1",
                        LocalDateTime.now()));
    }
}
