package com.elysia.mooc.event.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventErrorCode;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import com.elysia.mooc.event.domain.po.EventConsumeLogPO;
import com.elysia.mooc.event.mapper.EventConsumeLogMapper;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

/** 消费者幂等服务实现。 */
@Service
@RequiredArgsConstructor
public class ConsumerIdempotentServiceImpl implements ConsumerIdempotentService {

    private static final int MAX_ERROR_LENGTH = 1000;

    private final EventConsumeLogMapper eventConsumeLogMapper;
    private final PlatformTransactionManager transactionManager;

    /**
     * 判断事件是否已经成功消费。
     *
     * @param eventId 全局唯一事件 ID
     * @param consumerGroup 消费组
     * @return true 表示已有成功消费记录
     */
    @Override
    public boolean hasConsumed(String eventId, String consumerGroup) {
        return find(eventId, consumerGroup) != null;
    }

    /**
     * 在幂等保护下执行业务逻辑。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     * @param handler 真实业务逻辑
     * @return true 表示本次执行，false 表示重复消息被跳过
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeOnce(DomainEvent event, String consumerGroup, Runnable handler) {
        validate(event, consumerGroup);
        if (hasConsumed(event.getEventId(), consumerGroup)) {
            return false;
        }
        try {
            handler.run();
            recordSuccess(event, consumerGroup);
            return true;
        } catch (RuntimeException ex) {
            // 失败日志必须独立提交，否则外层消费事务回滚时会丢失失败原因，后台无法排查。
            recordFailureInNewTransaction(event, consumerGroup, ex);
            throw ex;
        }
    }

    /**
     * 记录消费成功。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     */
    @Override
    public void recordSuccess(DomainEvent event, String consumerGroup) {
        validate(event, consumerGroup);
        EventConsumeLogPO existing = findAny(event.getEventId(), consumerGroup);
        if (existing == null) {
            EventConsumeLogPO log = buildLog(event, consumerGroup, EventConsumeStatus.SUCCESS, null);
            eventConsumeLogMapper.insert(log);
            return;
        }
        existing.setStatus(EventConsumeStatus.SUCCESS);
        existing.setErrorMessage(null);
        existing.setCreateTime(LocalDateTime.now());
        eventConsumeLogMapper.updateById(existing);
    }

    /**
     * 记录消费失败但不写成功幂等记录。
     *
     * @param event 领域事件
     * @param consumerGroup 消费组
     * @param throwable 异常原因
     */
    @Override
    public void recordFailure(DomainEvent event, String consumerGroup, Throwable throwable) {
        validate(event, consumerGroup);
        EventConsumeLogPO existing = findAny(event.getEventId(), consumerGroup);
        if (existing != null && existing.getStatus() == EventConsumeStatus.SUCCESS) {
            return;
        }
        if (existing == null) {
            eventConsumeLogMapper.insert(buildLog(event, consumerGroup, EventConsumeStatus.FAILED,
                    toChineseError(throwable)));
            return;
        }
        existing.setStatus(EventConsumeStatus.FAILED);
        existing.setErrorMessage(toChineseError(throwable));
        existing.setCreateTime(LocalDateTime.now());
        eventConsumeLogMapper.updateById(existing);
    }

    private EventConsumeLogPO find(String eventId, String consumerGroup) {
        if (!StringUtils.hasText(eventId) || !StringUtils.hasText(consumerGroup)) {
            return null;
        }
        return eventConsumeLogMapper.selectOne(Wrappers.<EventConsumeLogPO>lambdaQuery()
                .eq(EventConsumeLogPO::getEventId, eventId.trim())
                .eq(EventConsumeLogPO::getConsumerGroup, consumerGroup.trim())
                .eq(EventConsumeLogPO::getStatus, EventConsumeStatus.SUCCESS));
    }

    private void recordFailureInNewTransaction(DomainEvent event, String consumerGroup, Throwable throwable) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> recordFailure(event, consumerGroup, throwable));
    }

    private EventConsumeLogPO findAny(String eventId, String consumerGroup) {
        return eventConsumeLogMapper.selectOne(Wrappers.<EventConsumeLogPO>lambdaQuery()
                .eq(EventConsumeLogPO::getEventId, eventId.trim())
                .eq(EventConsumeLogPO::getConsumerGroup, consumerGroup.trim()));
    }

    private EventConsumeLogPO buildLog(
            DomainEvent event,
            String consumerGroup,
            EventConsumeStatus status,
            String errorMessage) {
        EventConsumeLogPO log = new EventConsumeLogPO();
        log.setEventId(event.getEventId());
        log.setTopic(event.getTopic());
        log.setConsumerGroup(consumerGroup);
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setCreateTime(LocalDateTime.now());
        return log;
    }

    private void validate(DomainEvent event, String consumerGroup) {
        if (event == null || !StringUtils.hasText(event.getEventId()) || !StringUtils.hasText(event.getTopic())) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "消费事件缺少事件ID或Topic");
        }
        if (!StringUtils.hasText(consumerGroup)) {
            throw new BizException(EventErrorCode.EVENT_PARAM_INVALID, "消费组不能为空");
        }
    }

    private String toChineseError(Throwable throwable) {
        Throwable root = throwable;
        while (root != null && root.getCause() != null) {
            root = root.getCause();
        }
        String message = root != null && StringUtils.hasText(root.getMessage())
                ? root.getMessage()
                : "未知异常";
        String result = "消费失败：" + message;
        return result.length() > MAX_ERROR_LENGTH ? result.substring(0, MAX_ERROR_LENGTH) : result;
    }
}
