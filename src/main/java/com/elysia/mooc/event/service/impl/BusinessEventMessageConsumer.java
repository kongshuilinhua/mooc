package com.elysia.mooc.event.service.impl;

import com.elysia.mooc.common.enums.MessageType;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.AuditStatusChangedPayload;
import com.elysia.mooc.event.domain.payload.InteractionAnswerCreatedPayload;
import com.elysia.mooc.event.service.ConsumerIdempotentService;
import com.elysia.mooc.message.service.MessageCommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 业务事件到站内信的消费者。 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BusinessEventMessageConsumer {

    /** 消息中心消费组，配合 event_id + consumer_group 防止重复通知。 */
    public static final String MESSAGE_CENTER_CONSUMER_GROUP = "message-center";

    ConsumerIdempotentService consumerIdempotentService;
    MessageCommandService messageCommandService;
    ObjectMapper objectMapper;

    /**
     * 消费课程审核状态变更事件。
     *
     * @param message Kafka 消息 JSON
     */
    @KafkaListener(
            topics = EventTopicConstants.AUDIT_STATUS_CHANGED,
            groupId = "${mooc.event.message-consumer-group:message-center}",
            autoStartup = "${mooc.event.message-consumer-auto-startup:true}")
    public void onAuditStatusChanged(String message) {
        consumeAuditStatusChanged(readEvent(message));
    }

    /**
     * 消费问答回答创建事件。
     *
     * @param message Kafka 消息 JSON
     */
    @KafkaListener(
            topics = EventTopicConstants.INTERACTION_ANSWER_CREATED,
            groupId = "${mooc.event.message-consumer-group:message-center}",
            autoStartup = "${mooc.event.message-consumer-auto-startup:true}")
    public void onInteractionAnswerCreated(String message) {
        consumeInteractionAnswerCreated(readEvent(message));
    }

    /**
     * 处理审核事件并生成教师站内信。
     *
     * @param event 领域事件
     * @return true 表示本次实际生成消息，false 表示重复事件被跳过
     */
    public boolean consumeAuditStatusChanged(DomainEvent event) {
        return consumerIdempotentService.executeOnce(event, MESSAGE_CENTER_CONSUMER_GROUP, () -> {
            AuditStatusChangedPayload payload = convertPayload(event, AuditStatusChangedPayload.class);
            if (payload.teacherId() == null || !shouldNotifyAuditStatus(payload.afterStatus())) {
                return;
            }
            messageCommandService.sendToUser(
                    payload.operatorId(),
                    payload.teacherId(),
                    MessageType.AUDIT,
                    auditTitle(payload.afterStatus()),
                    auditContent(payload),
                    "/teacher/courses/" + payload.courseId());
        });
    }

    /**
     * 处理回答事件并生成提问者站内信。
     *
     * @param event 领域事件
     * @return true 表示本次实际生成消息，false 表示重复事件被跳过
     */
    public boolean consumeInteractionAnswerCreated(DomainEvent event) {
        return consumerIdempotentService.executeOnce(event, MESSAGE_CENTER_CONSUMER_GROUP, () -> {
            InteractionAnswerCreatedPayload payload = convertPayload(event, InteractionAnswerCreatedPayload.class);
            if (payload.questionUserId() == null || Objects.equals(payload.questionUserId(), payload.answerUserId())) {
                return;
            }
            messageCommandService.sendToUser(
                    payload.answerUserId(),
                    payload.questionUserId(),
                    MessageType.COURSE,
                    "你的课程提问有新回答",
                    "你提出的问题《" + safeTitle(payload.questionTitle()) + "》收到了新的回答。",
                    "/courses/" + payload.courseId() + "?questionId=" + payload.questionId());
        });
    }

    private DomainEvent readEvent(String message) {
        try {
            return objectMapper.readValue(message, DomainEvent.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("事件消息格式不正确", ex);
        }
    }

    private <T> T convertPayload(DomainEvent event, Class<T> type) {
        return objectMapper.convertValue(event.getPayload(), type);
    }

    private boolean shouldNotifyAuditStatus(CourseStatus status) {
        return status == CourseStatus.PUBLISHED || status == CourseStatus.REJECTED || status == CourseStatus.OFFLINE;
    }

    private String auditTitle(CourseStatus status) {
        if (status == CourseStatus.PUBLISHED) {
            return "课程审核通过";
        }
        if (status == CourseStatus.REJECTED) {
            return "课程审核未通过";
        }
        return "课程已下架";
    }

    private String auditContent(AuditStatusChangedPayload payload) {
        String courseTitle = safeTitle(payload.courseTitle());
        if (payload.afterStatus() == CourseStatus.PUBLISHED) {
            return "你的课程《" + courseTitle + "》已审核通过并发布。";
        }
        if (payload.afterStatus() == CourseStatus.REJECTED) {
            return "你的课程《" + courseTitle + "》审核未通过，原因：" + safeComment(payload.comment());
        }
        return "你的课程《" + courseTitle + "》已被下架，原因：" + safeComment(payload.comment());
    }

    private String safeTitle(String title) {
        return StringUtils.hasText(title) ? title.trim() : "未命名内容";
    }

    private String safeComment(String comment) {
        return StringUtils.hasText(comment) ? comment.trim() : "请查看审核记录";
    }
}
