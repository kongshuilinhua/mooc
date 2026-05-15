package com.elysia.mooc.event.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventErrorCode;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.enums.EventPublishStatus;
import com.elysia.mooc.event.domain.po.EventPublishLogPO;
import com.elysia.mooc.event.mapper.EventPublishLogMapper;
import com.elysia.mooc.event.service.KafkaMessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/** Kafka 事件发布状态流转测试。 */
@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private EventPublishLogMapper eventPublishLogMapper;

    @Mock
    private KafkaMessageSender kafkaMessageSender;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @InjectMocks
    private KafkaEventPublisher eventPublisher;

    @Test
    void publishShouldInsertPendingLogAndMarkSentWhenKafkaSuccess() throws Exception {
        AtomicReference<EventPublishLogPO> savedLog = mockInsertedLog();
        DomainEvent event = DomainEvent.builder()
                .eventId("evt-course-3001")
                .topic(EventTopicConstants.COURSE_PUBLISHED)
                .eventType("COURSE_PUBLISHED")
                .bizKey("course:3001")
                .payload(Map.of("courseId", 3001L))
                .build();

        String eventId = eventPublisher.publish(event);

        assertThat(eventId).isEqualTo("evt-course-3001");
        assertThat(savedLog.get().getStatus()).isEqualTo(EventPublishStatus.PENDING);
        assertThat(savedLog.get().getPayload()).contains("\"courseId\":3001");
        verify(kafkaMessageSender).send(
                eq(EventTopicConstants.COURSE_PUBLISHED),
                eq("evt-course-3001"),
                org.mockito.ArgumentMatchers.contains("\"eventId\":\"evt-course-3001\""));
        verify(eventPublishLogMapper).update(nullable(EventPublishLogPO.class), any(Wrapper.class));
    }

    @Test
    void publishShouldRecordFailedStateWhenKafkaUnavailable() throws Exception {
        AtomicReference<EventPublishLogPO> savedLog = mockInsertedLog();
        doThrow(new IllegalStateException("broker down"))
                .when(kafkaMessageSender).send(any(), any(), any());

        String eventId = eventPublisher.publish(DomainEvent.of(
                EventTopicConstants.MEDIA_DOCUMENT_UPLOADED,
                "MEDIA_DOCUMENT_UPLOADED",
                "media:20",
                Map.of("mediaId", 20L)));

        assertThat(eventId).isNotBlank();
        assertThat(savedLog.get().getRetryCount()).isZero();
        ArgumentCaptor<Wrapper<EventPublishLogPO>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(eventPublishLogMapper).update(nullable(EventPublishLogPO.class), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue().getSqlSet()).contains("status", "retry_count", "error_message");
    }

    @Test
    void retryShouldRejectAlreadySentEvent() {
        EventPublishLogPO log = new EventPublishLogPO();
        log.setId(1L);
        log.setEventId("evt-sent");
        log.setStatus(EventPublishStatus.SENT);
        when(eventPublishLogMapper.selectOne(any())).thenReturn(log);

        assertThatThrownBy(() -> eventPublisher.retry("evt-sent"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(EventErrorCode.EVENT_STATUS_INVALID.code());
    }

    @Test
    void retryShouldSendFailedEventAgain() throws Exception {
        EventPublishLogPO log = new EventPublishLogPO();
        log.setId(2L);
        log.setEventId("evt-failed");
        log.setTopic(EventTopicConstants.TRADE_ORDER_PAID);
        log.setEventType("ORDER_PAID");
        log.setPayload("{\"orderId\":21001}");
        log.setStatus(EventPublishStatus.FAILED);
        log.setRetryCount(1);
        when(eventPublishLogMapper.selectOne(any())).thenReturn(log);

        Boolean result = eventPublisher.retry("evt-failed");

        assertThat(result).isTrue();
        verify(kafkaMessageSender).send(eq(EventTopicConstants.TRADE_ORDER_PAID), eq("evt-failed"), any());
    }

    private AtomicReference<EventPublishLogPO> mockInsertedLog() {
        AtomicReference<EventPublishLogPO> savedLog = new AtomicReference<>();
        doAnswer(invocation -> {
            EventPublishLogPO log = invocation.getArgument(0);
            log.setId(100L);
            savedLog.set(log);
            return 1;
        }).when(eventPublishLogMapper).insert(any(EventPublishLogPO.class));
        when(eventPublishLogMapper.selectOne(any())).thenAnswer(invocation -> savedLog.get());
        when(eventPublishLogMapper.update(nullable(EventPublishLogPO.class), any(Wrapper.class))).thenReturn(1);
        return savedLog;
    }
}
