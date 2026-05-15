package com.elysia.mooc.event.service;

import com.elysia.mooc.course.domain.enums.CourseAuditAction;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.learning.domain.enums.LearningBehaviorType;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import java.time.LocalDateTime;

/** day11.5 业务事件发布门面，避免业务模块直接组装 Kafka 细节。 */
public interface BusinessEventPublisher {

    /**
     * 发布课程审核状态变更事件。
     *
     * @param courseId 课程 ID
     * @param courseTitle 课程标题
     * @param teacherId 课程教师 ID
     * @param beforeStatus 变更前状态
     * @param afterStatus 变更后状态
     * @param action 审核动作
     * @param operatorId 操作人 ID
     * @param comment 审核意见
     */
    void publishAuditStatusChanged(
            Long courseId,
            String courseTitle,
            Long teacherId,
            CourseStatus beforeStatus,
            CourseStatus afterStatus,
            CourseAuditAction action,
            Long operatorId,
            String comment);

    /**
     * 发布课程发布事件。
     *
     * @param courseId 课程 ID
     * @param courseTitle 课程标题
     * @param teacherId 课程教师 ID
     * @param publishTime 发布时间
     */
    void publishCoursePublished(Long courseId, String courseTitle, Long teacherId, LocalDateTime publishTime);

    /**
     * 发布问答回答创建事件。
     *
     * @param answerId 回答 ID
     * @param questionId 问题 ID
     * @param courseId 课程 ID
     * @param questionUserId 提问者用户 ID
     * @param answerUserId 回答者用户 ID
     * @param questionTitle 问题标题
     */
    void publishInteractionAnswerCreated(
            Long answerId,
            Long questionId,
            Long courseId,
            Long questionUserId,
            Long answerUserId,
            String questionTitle);

    /**
     * 发布学习行为创建事件。
     *
     * @param behaviorLogId 学习行为日志 ID
     * @param userId 学习用户 ID
     * @param courseId 课程 ID
     * @param sectionId 小节 ID
     * @param behaviorType 行为类型
     * @param positionSecond 播放位置
     * @param deltaSeconds 本次有效学习秒数
     */
    void publishLearningBehaviorCreated(
            Long behaviorLogId,
            Long userId,
            Long courseId,
            Long sectionId,
            LearningBehaviorType behaviorType,
            Integer positionSecond,
            Integer deltaSeconds);

    /**
     * 发布知识库文档上传事件。
     *
     * @param mediaFileId 媒资文件 ID
     * @param documentId 文档子表 ID
     * @param ownerId 上传用户 ID
     * @param originalName 原始文件名
     * @param fileUrl 文件访问地址
     * @param bizType 业务类型
     */
    void publishMediaDocumentUploaded(
            Long mediaFileId,
            Long documentId,
            Long ownerId,
            String originalName,
            String fileUrl,
            MediaBizType bizType);
}
