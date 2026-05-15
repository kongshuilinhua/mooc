package com.elysia.mooc.event.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.enums.EventConsumeStatus;
import com.elysia.mooc.event.domain.po.EventConsumeLogPO;
import com.elysia.mooc.event.mapper.EventConsumeLogMapper;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/** 消费幂等服务测试。 */
@ExtendWith(MockitoExtension.class)
class ConsumerIdempotentServiceImplTest {

    @Mock
    private EventConsumeLogMapper eventConsumeLogMapper;

    private ConsumerIdempotentServiceImpl consumerIdempotentService;

    @BeforeEach
    void setUp() {
        PlatformTransactionManager transactionManager = new TestTransactionManager();
        consumerIdempotentService = new ConsumerIdempotentServiceImpl(eventConsumeLogMapper, transactionManager);
    }

    @Test
    void executeOnceShouldRunHandlerAndRecordSuccess() {
        when(eventConsumeLogMapper.selectOne(any())).thenReturn(null);
        DomainEvent event = event();
        AtomicInteger counter = new AtomicInteger();

        boolean executed = consumerIdempotentService.executeOnce(event, "message-center", counter::incrementAndGet);

        assertThat(executed).isTrue();
        assertThat(counter).hasValue(1);
        ArgumentCaptor<EventConsumeLogPO> captor = ArgumentCaptor.forClass(EventConsumeLogPO.class);
        verify(eventConsumeLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventConsumeStatus.SUCCESS);
        assertThat(captor.getValue().getConsumerGroup()).isEqualTo("message-center");
    }

    @Test
    void executeOnceShouldSkipWhenSuccessLogExists() {
        EventConsumeLogPO consumed = new EventConsumeLogPO();
        consumed.setStatus(EventConsumeStatus.SUCCESS);
        when(eventConsumeLogMapper.selectOne(any())).thenReturn(consumed);
        AtomicInteger counter = new AtomicInteger();

        boolean executed = consumerIdempotentService.executeOnce(event(), "message-center", counter::incrementAndGet);

        assertThat(executed).isFalse();
        assertThat(counter).hasValue(0);
        verify(eventConsumeLogMapper, never()).insert(any(EventConsumeLogPO.class));
    }

    @Test
    void executeOnceShouldRecordFailureWithoutSuccessIdempotentLog() {
        when(eventConsumeLogMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> consumerIdempotentService.executeOnce(
                event(),
                "message-center",
                () -> {
                    throw new IllegalStateException("模拟消费失败");
                }))
                .isInstanceOf(IllegalStateException.class);

        ArgumentCaptor<EventConsumeLogPO> captor = ArgumentCaptor.forClass(EventConsumeLogPO.class);
        verify(eventConsumeLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventConsumeStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).contains("消费失败：模拟消费失败");
    }

    private DomainEvent event() {
        return DomainEvent.builder()
                .eventId("evt-answer-1")
                .topic(EventTopicConstants.INTERACTION_ANSWER_CREATED)
                .eventType("INTERACTION_ANSWER_CREATED")
                .bizKey("answer:1")
                .payload(Map.of("answerId", 1L))
                .build();
    }

    /** 测试用事务管理器，只验证 REQUIRES_NEW 代码路径，不连接真实数据库事务。 */
    private static class TestTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition)
                throws TransactionException {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        }
    }
}
