package com.elysia.mooc.event.domain.payload;

import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;

/** 学习行为创建事件载荷。 */
public record LearningBehaviorCreatedPayload(
        Long behaviorLogId,
        Long userId,
        Long courseId,
        Long sectionId,
        LearningBehaviorType behaviorType,
        Integer positionSecond,
        Integer deltaSeconds) {
}
