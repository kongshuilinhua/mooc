package com.elysia.mooc.event.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 事件发布日志查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventPublishLogQuery extends PageQuery {

    /** 搜索关键字，匹配事件 ID、Topic、事件类型、业务键和错误信息。 */
    private String keyword;

    /** 发布状态筛选。 */
    private EventPublishStatus status;

    /** 事件类型筛选。 */
    private String eventType;

    /** Topic 筛选。 */
    private String topic;

    /** 排序字段，支持 createTime、updateTime、retryCount、nextRetryTime、id。 */
    private String sortBy;

    /** 是否升序，默认 false。 */
    private Boolean isAsc = Boolean.FALSE;
}
