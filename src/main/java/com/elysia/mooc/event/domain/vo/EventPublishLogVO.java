package com.elysia.mooc.event.domain.vo;

import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 事件发布日志视图对象。 */
@Data
public class EventPublishLogVO {

    /** 主键 ID。 */
    private Long id;

    /** 全局唯一事件 ID。 */
    private String eventId;

    /** Kafka Topic。 */
    private String topic;

    /** 事件类型。 */
    private String eventType;

    /** 业务键。 */
    private String bizKey;

    /** JSON 字符串载荷。 */
    private String payload;

    /** 发布状态。 */
    private EventPublishStatus status;

    /** 重试次数。 */
    private Integer retryCount;

    /** 下次重试时间。 */
    private LocalDateTime nextRetryTime;

    /** 错误信息。 */
    private String errorMessage;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
