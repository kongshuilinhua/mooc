package com.elysia.mooc.event.service.impl;

import com.elysia.mooc.event.service.KafkaMessageSender;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** 基于 Spring KafkaTemplate 的消息发送适配器。 */
@Component
@RequiredArgsConstructor
public class KafkaTemplateMessageSender implements KafkaMessageSender {

    private static final long SEND_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void send(String topic, String key, String message) throws Exception {
        kafkaTemplate.send(topic, key, message).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
