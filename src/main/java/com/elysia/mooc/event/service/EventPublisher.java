package com.elysia.mooc.event.service;

import com.elysia.mooc.event.domain.DomainEvent;

/** 事件发布服务，统一处理事件落库、事务后发送和失败补偿。 */
public interface EventPublisher {

    /**
     * 发布领域事件。
     *
     * @param event 领域事件，必须包含 Topic、事件类型和载荷
     * @return 全局唯一事件 ID
     */
    String publish(DomainEvent event);

    /**
     * 手动重试指定事件。
     *
     * @param eventId 全局唯一事件 ID
     * @return 发送成功返回 true
     */
    Boolean retry(String eventId);

    /**
     * 重试一批到期失败或待发送事件。
     *
     * @param limit 单次重试数量
     * @return 实际尝试重试的事件数量
     */
    int retryDueEvents(int limit);
}
