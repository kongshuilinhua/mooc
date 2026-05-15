package com.elysia.mooc.event.service;

import com.elysia.mooc.event.domain.DomainEvent;

/** 消费者幂等服务，使用 eventId 和 consumerGroup 防止重复执行业务。 */
public interface ConsumerIdempotentService {

    /**
     * 判断事件是否已经被指定消费组成功消费。
     *
     * @param eventId 全局唯一事件 ID
     * @param consumerGroup 消费组
     * @return true 表示已有成功消费记录
     */
    boolean hasConsumed(String eventId, String consumerGroup);

    /**
     * 在幂等保护下执行业务逻辑。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     * @param handler 真实业务逻辑
     * @return true 表示本次实际执行业务，false 表示已有成功记录被跳过
     */
    boolean executeOnce(DomainEvent event, String consumerGroup, Runnable handler);

    /**
     * 记录消费成功。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     */
    void recordSuccess(DomainEvent event, String consumerGroup);

    /**
     * 记录消费失败，但不写成功幂等记录。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     * @param throwable 异常原因
     */
    void recordFailure(DomainEvent event, String consumerGroup, Throwable throwable);
}
