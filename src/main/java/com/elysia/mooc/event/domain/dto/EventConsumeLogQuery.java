package com.elysia.mooc.event.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 事件消费日志查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventConsumeLogQuery extends PageQuery {

    /** 搜索关键字，匹配事件 ID、Topic、消费组和错误信息。 */
    private String keyword;

    /** 消费状态筛选。 */
    private EventConsumeStatus status;

    /** 事件 ID 筛选。 */
    private String eventId;

    /** 消费组筛选。 */
    private String consumerGroup;

    /** Topic 筛选。 */
    private String topic;

    /** 排序字段，支持 createTime、id。 */
    private String sortBy;

    /** 是否升序，默认 false。 */
    private Boolean isAsc = Boolean.FALSE;
}
