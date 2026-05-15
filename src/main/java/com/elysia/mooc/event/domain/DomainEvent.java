package com.elysia.mooc.event.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/** 统一领域事件对象，作为 Kafka 消息和发布日志的稳定结构。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

    /** 全局唯一事件 ID。 */
    private String eventId;

    /** Kafka Topic。 */
    private String topic;

    /** 事件类型。 */
    private String eventType;

    /** 业务键，例如 course:3001。 */
    private String bizKey;

    /** 事件载荷，必须是明确 DTO 或可序列化对象。 */
    private Object payload;

    /** 事件创建时间。 */
    private LocalDateTime occurredAt;

    /**
     * 创建带默认事件 ID 和时间的事件对象。
     *
     * @param topic Kafka Topic
     * @param eventType 事件类型
     * @param bizKey 业务键，可为空
     * @param payload 事件载荷
     * @return 领域事件
     */
    public static DomainEvent of(String topic, String eventType, String bizKey, Object payload) {
        return DomainEvent.builder()
                .eventId(UUID.randomUUID().toString().replace("-", ""))
                .topic(topic)
                .eventType(eventType)
                .bizKey(bizKey)
                .payload(payload)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 补齐事件默认字段，允许调用方传入自定义 eventId。
     *
     * @return 当前事件对象
     */
    public DomainEvent fillDefaults() {
        if (!StringUtils.hasText(eventId)) {
            eventId = UUID.randomUUID().toString().replace("-", "");
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        return this;
    }
}
