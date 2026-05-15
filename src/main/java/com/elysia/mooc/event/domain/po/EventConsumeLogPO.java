package com.elysia.mooc.event.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 事件消费日志实体，映射 event_consume_log 表。 */
@Data
@TableName("event_consume_log")
public class EventConsumeLogPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
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
