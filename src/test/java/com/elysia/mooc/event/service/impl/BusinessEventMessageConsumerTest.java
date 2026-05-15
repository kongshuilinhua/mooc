package com.elysia.mooc.event.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.AuditStatusChangedPayload;
import com.elysia.mooc.event.domain.payload.InteractionAnswerCreatedPayload;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.elysia.mooc.message.service.MessageCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 业务事件消息消费者测试。 */
@ExtendWith(MockitoExtension.class)
class BusinessEventMessageConsumerTest {

    @Mock
    private ConsumerIdempotentService consumerIdempotentService;

    @Mock
    private MessageCommandService messageCommandService;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private BusinessEventMessageConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new BusinessEventMessageConsumer(
                consumerIdempotentService,
                messageCommandService,
                objectMapper);
    }

    @Test
    void consumeAuditStatusChangedShouldCreateAuditMessageOnce() {
        doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return true;
        }).when(consumerIdempotentService).executeOnce(any(), eq("message-center"), any());
        DomainEvent event = DomainEvent.of(
                EventTopicConstants.AUDIT_STATUS_CHANGED,
                EventTopicConstants.AUDIT_STATUS_CHANGED,
                "course:3001",
                new AuditStatusChangedPayload(
                        3001L,
                        "AI 入门",
                        2L,
                        CourseStatus.PENDING,
                        CourseStatus.PUBLISHED,
                        CourseAuditAction.APPROVE,
                        1L,
                        "通过"));

        boolean executed = consumer.consumeAuditStatusChanged(event);

        assertThat(executed).isTrue();
        verify(messageCommandService).sendToUser(
                1L,
                2L,
                MessageType.AUDIT,
                "课程审核通过",
                "你的课程《AI 入门》已审核通过并发布。",
                "/teacher/courses/3001");
    }

    @Test
    void consumeInteractionAnswerCreatedShouldSkipSelfAnswerMessage() {
        doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return true;
        }).when(consumerIdempotentService).executeOnce(any(), eq("message-center"), any());
        DomainEvent event = DomainEvent.of(
                EventTopicConstants.INTERACTION_ANSWER_CREATED,
                EventTopicConstants.INTERACTION_ANSWER_CREATED,
                "answer:9101",
                new InteractionAnswerCreatedPayload(9101L, 9001L, 3001L, 3L, 3L, "如何学习"));

        consumer.consumeInteractionAnswerCreated(event);

        verify(messageCommandService, never()).sendToUser(any(), any(), any(), any(), any(), any());
    }

    @Test
    void onInteractionAnswerCreatedShouldParseKafkaJsonAndCreateMessage() throws Exception {
        doAnswer(invocation -> {
            invocation.getArgument(2, Runnable.class).run();
            return true;
        }).when(consumerIdempotentService).executeOnce(any(), eq("message-center"), any());
        DomainEvent event = DomainEvent.builder()
                .eventId("evt-answer-1")
                .topic(EventTopicConstants.INTERACTION_ANSWER_CREATED)
                .eventType(EventTopicConstants.INTERACTION_ANSWER_CREATED)
                .bizKey("answer:9101")
                .payload(Map.of(
                        "answerId", 9101L,
                        "questionId", 9001L,
                        "courseId", 3001L,
                        "questionUserId", 3L,
                        "answerUserId", 2L,
                        "questionTitle", "如何学习"))
                .build();

        consumer.onInteractionAnswerCreated(objectMapper.writeValueAsString(event));

        verify(messageCommandService).sendToUser(
                2L,
                3L,
                MessageType.COURSE,
                "你的课程提问有新回答",
                "你提出的问题《如何学习》收到了新的回答。",
                "/courses/3001?questionId=9001");
    }

    @Test
    void consumeAuditStatusChangedShouldSkipDuplicateSuccess() {
        when(consumerIdempotentService.executeOnce(any(), eq("message-center"), any())).thenReturn(false);
        DomainEvent event = DomainEvent.of(
                EventTopicConstants.AUDIT_STATUS_CHANGED,
                EventTopicConstants.AUDIT_STATUS_CHANGED,
                "course:3001",
                new AuditStatusChangedPayload(
                        3001L,
                        "AI 入门",
                        2L,
                        CourseStatus.PENDING,
                        CourseStatus.REJECTED,
                        CourseAuditAction.REJECT,
                        1L,
                        "资料不完整"));

        boolean executed = consumer.consumeAuditStatusChanged(event);

        assertThat(executed).isFalse();
        verify(messageCommandService, never()).sendToUser(any(), any(), any(), any(), any(), any());
    }
}
