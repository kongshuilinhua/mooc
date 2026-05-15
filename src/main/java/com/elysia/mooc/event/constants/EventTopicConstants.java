package com.elysia.mooc.event.constants;

/** Kafka Topic 常量，统一维护事件主题名称。 */
public final class EventTopicConstants {

    /** 学习行为创建事件。 */
    public static final String LEARNING_BEHAVIOR_CREATED = "mooc.learning.behavior.created";

    /** 课程资料上传事件。 */
    public static final String MEDIA_DOCUMENT_UPLOADED = "mooc.media.document.uploaded";

    /** 课程资料安全扫描完成事件。 */
    public static final String MEDIA_DOCUMENT_SCANNED = "mooc.media.document.scanned";

    /** 知识库向量化请求事件。 */
    public static final String KNOWLEDGE_EMBEDDING_REQUESTED = "mooc.knowledge.embedding.requested";

    /** 知识库向量化完成事件。 */
    public static final String KNOWLEDGE_EMBEDDING_COMPLETED = "mooc.knowledge.embedding.completed";

    /** AI 用量记录事件。 */
    public static final String AI_USAGE_RECORDED = "mooc.ai.usage.recorded";

    /** 订单支付成功事件。 */
    public static final String TRADE_ORDER_PAID = "mooc.trade.order.paid";

    /** 课程发布事件。 */
    public static final String COURSE_PUBLISHED = "mooc.course.published";

    /** 审核状态变更事件。 */
    public static final String AUDIT_STATUS_CHANGED = "mooc.audit.status.changed";

    /** 问答回答创建事件。 */
    public static final String INTERACTION_ANSWER_CREATED = "mooc.interaction.answer.created";

    /** 学习行为死信 Topic。 */
    public static final String LEARNING_BEHAVIOR_CREATED_DLQ = LEARNING_BEHAVIOR_CREATED + ".dlq";

    /** 资料上传死信 Topic。 */
    public static final String MEDIA_DOCUMENT_UPLOADED_DLQ = MEDIA_DOCUMENT_UPLOADED + ".dlq";

    /** 知识库向量化请求死信 Topic。 */
    public static final String KNOWLEDGE_EMBEDDING_REQUESTED_DLQ = KNOWLEDGE_EMBEDDING_REQUESTED + ".dlq";

    /** 课程发布死信 Topic。 */
    public static final String COURSE_PUBLISHED_DLQ = COURSE_PUBLISHED + ".dlq";

    /** 订单支付死信 Topic。 */
    public static final String TRADE_ORDER_PAID_DLQ = TRADE_ORDER_PAID + ".dlq";

    private EventTopicConstants() {
    }
}
