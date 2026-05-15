package com.elysia.mooc.event.domain.vo;

import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 事件消费日志视图对象。 */
@Data
public class EventConsumeLogVO {

    /** 主键 ID。 */
    private Long id;

    /** 全局唯一事件 ID。 */
    private String eventId;

    /** Kafka Topic。 */
    private String topic;

    /** 消费组。 */
    private String consumerGroup;

    /** 消费状态。 */
    private EventConsumeStatus status;

    /** 错误信息。 */
    private String errorMessage;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
