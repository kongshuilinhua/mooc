package com.elysia.mooc.event.service;

/** Kafka 消息发送适配器，便于测试环境替换真实 KafkaTemplate。 */
public interface KafkaMessageSender {

    /**
     * 发送 Kafka 消息。
     *
     * @param topic Kafka Topic
     * @param key 消息 Key，使用 eventId 便于分区内有序
     * @param message JSON 消息体
     * @throws Exception Kafka 客户端发送异常
     */
    void send(String topic, String key, String message) throws Exception;
}
