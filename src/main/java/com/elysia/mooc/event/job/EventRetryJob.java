package com.elysia.mooc.event.job;

import com.elysia.mooc.event.service.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 事件失败补偿任务，用于 Kafka 临时不可用后的重试。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetryJob {

    private static final int BATCH_SIZE = 50;

    private final EventPublisher eventPublisher;

    /**
     * 定时重试到期事件。
     */
    @Scheduled(
            fixedDelayString = "${mooc.event.retry-fixed-delay:60000}",
            initialDelayString = "${mooc.event.retry-initial-delay:60000}")
    public void retryDueEvents() {
        // Kafka 不可用时事件已经落库，定时任务只做补偿发送，不影响主业务提交。
        int count = eventPublisher.retryDueEvents(BATCH_SIZE);
        if (count > 0) {
            log.info("事件补偿重试完成，本次处理 {} 条", count);
        }
    }
}
