package com.elysia.mooc.trade.service.impl;

import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.OrderPaidPayload;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.elysia.mooc.learning.service.LearningService;
import com.elysia.mooc.trade.constants.TradeConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** 订单支付成功事件消费者。 */
@Component
@RequiredArgsConstructor
public class OrderPaidEventConsumer {

    private final ConsumerIdempotentService consumerIdempotentService;
    private final LearningService learningService;
    private final ObjectMapper objectMapper;

    /**
     * 消费订单支付成功 Kafka 消息。
     *
     * @param message Kafka 消息 JSON
     */
    @KafkaListener(
            topics = EventTopicConstants.TRADE_ORDER_PAID,
            groupId = "${mooc.event.trade-learning-consumer-group:trade-learning-grant}",
            autoStartup = "${mooc.event.message-consumer-auto-startup:true}")
    public void onOrderPaid(String message) {
        consumeOrderPaid(readEvent(message));
    }

    /**
     * 发放支付成功后的课程学习权益。
     *
     * @param event 订单支付成功事件
     * @return true 表示本次实际执行，false 表示重复事件被跳过
     */
    public boolean consumeOrderPaid(DomainEvent event) {
        return consumerIdempotentService.executeOnce(event, TradeConstants.ORDER_PAID_LEARNING_CONSUMER_GROUP, () -> {
            OrderPaidPayload payload = objectMapper.convertValue(event.getPayload(), OrderPaidPayload.class);
            learningService.grantPurchasedCourse(payload.userId(), payload.courseId());
        });
    }

    private DomainEvent readEvent(String message) {
        try {
            return objectMapper.readValue(message, DomainEvent.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("订单支付事件消息格式不正确", ex);
        }
    }
}
