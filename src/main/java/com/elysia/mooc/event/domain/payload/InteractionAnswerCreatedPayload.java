package com.elysia.mooc.event.domain.payload;

/** 问答回答创建事件载荷。 */
public record InteractionAnswerCreatedPayload(
        Long answerId,
        Long questionId,
        Long courseId,
        Long questionUserId,
        Long answerUserId,
        String questionTitle) {
}
