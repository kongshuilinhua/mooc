package com.elysia.mooc.event.service.impl;

import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.domain.payload.AuditStatusChangedPayload;
import com.elysia.mooc.event.domain.payload.CoursePublishedPayload;
import com.elysia.mooc.event.domain.payload.InteractionAnswerCreatedPayload;
import com.elysia.mooc.event.domain.payload.LearningBehaviorCreatedPayload;
import com.elysia.mooc.event.domain.payload.MediaDocumentUploadedPayload;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 业务事件发布门面默认实现。 */
@Service
@RequiredArgsConstructor
public class BusinessEventPublisherImpl implements BusinessEventPublisher {

    private final EventPublisher eventPublisher;

    @Override
    public void publishAuditStatusChanged(
            Long courseId,
            String courseTitle,
            Long teacherId,
            CourseStatus beforeStatus,
            CourseStatus afterStatus,
            CourseAuditAction action,
            Long operatorId,
            String comment) {
        AuditStatusChangedPayload payload = new AuditStatusChangedPayload(
                courseId, courseTitle, teacherId, beforeStatus, afterStatus, action, operatorId, comment);
        publish(EventTopicConstants.AUDIT_STATUS_CHANGED, "course:" + courseId, payload);
    }

    @Override
    public void publishCoursePublished(Long courseId, String courseTitle, Long teacherId, LocalDateTime publishTime) {
        CoursePublishedPayload payload = new CoursePublishedPayload(courseId, courseTitle, teacherId, publishTime);
        publish(EventTopicConstants.COURSE_PUBLISHED, "course:" + courseId, payload);
    }

    @Override
    public void publishInteractionAnswerCreated(
            Long answerId,
            Long questionId,
            Long courseId,
            Long questionUserId,
            Long answerUserId,
            String questionTitle) {
        InteractionAnswerCreatedPayload payload = new InteractionAnswerCreatedPayload(
                answerId, questionId, courseId, questionUserId, answerUserId, questionTitle);
        publish(EventTopicConstants.INTERACTION_ANSWER_CREATED, "answer:" + answerId, payload);
    }

    @Override
    public void publishLearningBehaviorCreated(
            Long behaviorLogId,
            Long userId,
            Long courseId,
            Long sectionId,
            LearningBehaviorType behaviorType,
            Integer positionSecond,
            Integer deltaSeconds) {
        LearningBehaviorCreatedPayload payload = new LearningBehaviorCreatedPayload(
                behaviorLogId, userId, courseId, sectionId, behaviorType, positionSecond, deltaSeconds);
        publish(EventTopicConstants.LEARNING_BEHAVIOR_CREATED, "learning_behavior:" + behaviorLogId, payload);
    }

    @Override
    public void publishMediaDocumentUploaded(
            Long mediaFileId,
            Long documentId,
            Long ownerId,
            String originalName,
            String fileUrl,
            MediaBizType bizType) {
        MediaDocumentUploadedPayload payload = new MediaDocumentUploadedPayload(
                mediaFileId, documentId, ownerId, originalName, fileUrl, bizType);
        publish(EventTopicConstants.MEDIA_DOCUMENT_UPLOADED, "media_file:" + mediaFileId, payload);
    }

    private void publish(String topic, String bizKey, Object payload) {
        eventPublisher.publish(DomainEvent.of(topic, topic, bizKey, payload));
    }
}
