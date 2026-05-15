package com.elysia.mooc.event.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 事件发布日志实体，映射 event_publish_log 表。 */
@Data
@TableName("event_publish_log")
public class EventPublishLogPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
