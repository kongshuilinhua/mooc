package com.elysia.mooc.knowledge.service.impl;

import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.MediaDocumentUploadedPayload;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** 文档上传事件消费者，用于自动触发解析切片。 */
@Component
@RequiredArgsConstructor
public class DocumentParseConsumer {

    /** 文档解析消费组。 */
    public static final String CONSUMER_GROUP = "knowledge-document-parser";

    private final ConsumerIdempotentService consumerIdempotentService;
    private final KnowledgeDocumentParseServiceImpl knowledgeDocumentParseService;
    private final ObjectMapper objectMapper;

    /**
     * 消费文档上传事件。
     *
     * @param message Kafka 消息 JSON
     */
    @KafkaListener(
            topics = EventTopicConstants.MEDIA_DOCUMENT_UPLOADED,
            groupId = "${mooc.knowledge.parse-consumer-group:knowledge-document-parser}",
            autoStartup = "${mooc.knowledge.parse-consumer-auto-startup:${mooc.event.message-consumer-auto-startup:true}}")
    public void onMediaDocumentUploaded(String message) {
        DomainEvent event = readEvent(message);
        consumerIdempotentService.executeOnce(event, CONSUMER_GROUP, () -> {
            MediaDocumentUploadedPayload payload = objectMapper.convertValue(
                    event.getPayload(), MediaDocumentUploadedPayload.class);
            knowledgeDocumentParseService.parseUploadedDocument(payload);
        });
    }

    private DomainEvent readEvent(String message) {
        try {
            return objectMapper.readValue(message, DomainEvent.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("文档解析事件消息格式不正确", ex);
        }
    }
}
