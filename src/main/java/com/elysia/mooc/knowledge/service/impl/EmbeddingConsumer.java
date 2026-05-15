package com.elysia.mooc.knowledge.service.impl;

import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** 知识库向量化请求消费者。 */
@Component
@RequiredArgsConstructor
public class EmbeddingConsumer {

    /** 向量化消费组。 */
    public static final String CONSUMER_GROUP = "knowledge-embedding";

    private final ConsumerIdempotentService consumerIdempotentService;
    private final EmbeddingServiceImpl embeddingService;
    private final ObjectMapper objectMapper;

    /**
     * 消费向量化请求事件。
     *
     * @param message Kafka 消息 JSON
     */
    @KafkaListener(
            topics = EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED,
            groupId = "${mooc.knowledge.embedding-consumer-group:knowledge-embedding}",
            autoStartup = "${mooc.knowledge.embedding-consumer-auto-startup:${mooc.event.message-consumer-auto-startup:true}}")
    public void onEmbeddingRequested(String message) {
        DomainEvent event = readEvent(message);
        consumerIdempotentService.executeOnce(event, CONSUMER_GROUP, () -> {
            KnowledgeEmbeddingRequestedPayload payload = objectMapper.convertValue(
                    event.getPayload(), KnowledgeEmbeddingRequestedPayload.class);
            embeddingService.handleEmbeddingRequested(payload);
        });
    }

    private DomainEvent readEvent(String message) {
        try {
            return objectMapper.readValue(message, DomainEvent.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("向量化事件消息格式不正确", ex);
        }
    }
}
