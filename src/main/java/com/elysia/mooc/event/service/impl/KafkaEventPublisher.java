package com.elysia.mooc.event.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventErrorCode;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import com.elysia.mooc.event.domain.po.EventPublishLogPO;
import com.elysia.mooc.event.mapper.EventPublishLogMapper;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.event.service.KafkaMessageSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

/** Kafka 事件发布服务实现。 */
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private static final int DEFAULT_RETRY_LIMIT = 50;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final EventPublishLogMapper eventPublishLogMapper;
    private final KafkaMessageSender kafkaMessageSender;
    private final ObjectMapper objectMapper;

    /**
     * 发布领域事件。
     *
     * @param event 领域事件，必须包含 Topic、事件类型和载荷
     * @return 全局唯一事件 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publish(DomainEvent event) {
        DomainEvent safeEvent = validateAndFill(event);
        EventPublishLogPO log = toPendingLog(safeEvent);
        try {
            eventPublishLogMapper.insert(log);
        } catch (DuplicateKeyException ex) {
            throw new BizException(EventErrorCode.EVENT_DUPLICATED, "事件ID已存在，请勿重复发布");
        }
        registerAfterCommitSend(log.getEventId());
        return log.getEventId();
    }

    /**
     * 手动重试指定事件。
     *
     * @param eventId 全局唯一事件 ID
     * @return 发送成功返回 true
     */
    @Override
    public Boolean retry(String eventId) {
        EventPublishLogPO log = getByEventId(eventId);
        if (log.getStatus() == EventPublishStatus.SENT) {
            throw new BizException(EventErrorCode.EVENT_STATUS_INVALID, "已发送事件不能重复重试");
        }
        boolean success = sendAndUpdate(log);
        if (!success) {
            throw new BizException(EventErrorCode.EVENT_SEND_FAILED, "事件重试失败，已记录错误原因");
        }
        return true;
    }

    /**
     * 重试一批到期事件。
     *
     * @param limit 单次重试数量
     * @return 实际尝试重试的事件数量
     */
    @Override
    public int retryDueEvents(int limit) {
        int safeLimit = limit <= 0 ? DEFAULT_RETRY_LIMIT : limit;
        List<EventPublishLogPO> logs = eventPublishLogMapper.selectList(Wrappers.<EventPublishLogPO>lambdaQuery()
                .in(EventPublishLogPO::getStatus, EventPublishStatus.PENDING, EventPublishStatus.FAILED)
                .and(wrapper -> wrapper.isNull(EventPublishLogPO::getNextRetryTime)
                        .or().le(EventPublishLogPO::getNextRetryTime, LocalDateTime.now()))
                .orderByAsc(EventPublishLogPO::getNextRetryTime)
                .orderByAsc(EventPublishLogPO::getId)
                .last("LIMIT " + safeLimit));
        logs.forEach(this::sendAndUpdate);
        return logs.size();
    }

    /**
     * 事务提交后发送 Kafka，避免数据库事务回滚但消息已经发出的不一致问题。
     *
     * @param eventId 全局唯一事件 ID
     */
    private void registerAfterCommitSend(String eventId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendByEventId(eventId);
                }
            });
            return;
        }
        sendByEventId(eventId);
    }

    private void sendByEventId(String eventId) {
        EventPublishLogPO log = eventPublishLogMapper.selectOne(Wrappers.<EventPublishLogPO>lambdaQuery()
                .eq(EventPublishLogPO::getEventId, eventId));
        if (log != null && log.getStatus() != EventPublishStatus.SENT) {
            sendAndUpdate(log);
        }
    }

    private boolean sendAndUpdate(EventPublishLogPO log) {
        try {
            String message = buildKafkaMessage(log);
            kafkaMessageSender.send(log.getTopic(), log.getEventId(), message);
            eventPublishLogMapper.update(null, Wrappers.<EventPublishLogPO>update()
                    .eq("id", log.getId())
                    .set("status", EventPublishStatus.SENT)
                    .set("error_message", null)
                    .set("next_retry_time", null));
            return true;
        } catch (Exception ex) {
            int nextRetryCount = (log.getRetryCount() == null ? 0 : log.getRetryCount()) + 1;
            eventPublishLogMapper.update(null, Wrappers.<EventPublishLogPO>update()
                    .eq("id", log.getId())
                    .set("status", EventPublishStatus.FAILED)
                    .set("retry_count", nextRetryCount)
                    .set("next_retry_time", nextRetryTime(nextRetryCount))
                    .set("error_message", toChineseError(ex)));
            return false;
        }
    }

    private EventPublishLogPO getByEventId(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件ID不能为空");
        }
        EventPublishLogPO log = eventPublishLogMapper.selectOne(Wrappers.<EventPublishLogPO>lambdaQuery()
                .eq(EventPublishLogPO::getEventId, eventId.trim()));
        if (log == null) {
            throw new BizException(EventErrorCode.EVENT_NOT_FOUND);
        }
        return log;
    }

    private DomainEvent validateAndFill(DomainEvent event) {
        if (event == null) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件对象不能为空");
        }
        event.fillDefaults();
        if (!StringUtils.hasText(event.getTopic())) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件Topic不能为空");
        }
        if (!StringUtils.hasText(event.getEventType())) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件类型不能为空");
        }
        if (event.getPayload() == null) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件载荷不能为空");
        }
        return event;
    }

    private EventPublishLogPO toPendingLog(DomainEvent event) {
        EventPublishLogPO log = new EventPublishLogPO();
        log.setEventId(event.getEventId());
        log.setTopic(event.getTopic());
        log.setEventType(event.getEventType());
        log.setBizKey(event.getBizKey());
        log.setPayload(toPayloadJson(event.getPayload()));
        log.setStatus(EventPublishStatus.PENDING);
        log.setRetryCount(0);
        log.setDeleted(0);
        return log;
    }

    private String toPayloadJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "事件载荷必须可以序列化为JSON");
        }
    }

    private String buildKafkaMessage(EventPublishLogPO log) throws Exception {
        JsonNode payloadNode = objectMapper.readTree(log.getPayload());
        DomainEvent event = DomainEvent.builder()
                .eventId(log.getEventId())
                .topic(log.getTopic())
                .eventType(log.getEventType())
                .bizKey(log.getBizKey())
                .payload(payloadNode)
                .occurredAt(log.getCreateTime() == null ? LocalDateTime.now() : log.getCreateTime())
                .build();
        return objectMapper.writeValueAsString(event);
    }

    private LocalDateTime nextRetryTime(Integer retryCount) {
        long minutes = Math.min(30L, Math.max(1L, retryCount == null ? 1L : retryCount.longValue()));
        return LocalDateTime.now().plusMinutes(minutes);
    }

    private String toChineseError(Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String message = StringUtils.hasText(root.getMessage()) ? root.getMessage() : root.getClass().getSimpleName();
        String result = "Kafka 发送失败：" + message;
        return result.length() > MAX_ERROR_LENGTH ? result.substring(0, MAX_ERROR_LENGTH) : result;
    }
}
