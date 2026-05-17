package com.elysia.mooc.ops.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 阶段一生产化巡检只读聚合 Mapper。 */
@Mapper
public interface ProductionCheckMapper {

    /**
     * 统计用户数量。
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0")
    Long countUsers();

    /**
     * 统计绑定指定角色的演示账号数量。
     * @param username 演示账号用户名
     * @param roleCode 角色编码
     * @return 匹配数量
     */
    @Select("""
            SELECT COUNT(*)
            FROM sys_user u
            JOIN sys_user_role ur ON ur.user_id = u.id
            JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE u.deleted = 0
              AND u.status = 1
              AND u.username = #{username}
              AND r.code = #{roleCode}
              AND r.status = 1
            """)
    Long countUserRole(@Param("username") String username, @Param("roleCode") String roleCode);

    /**
     * 统计课程分类数量。
     * @return 分类数量
     */
    @Select("SELECT COUNT(*) FROM course_category WHERE deleted = 0 AND status = 1")
    Long countCourseCategories();

    /**
     * 统计课程标签数量。
     * @return 标签数量
     */
    @Select("SELECT COUNT(*) FROM course_tag WHERE deleted = 0 AND status = 1")
    Long countCourseTags();

    /**
     * 统计课程数量。
     * @return 课程总数
     */
    @Select("SELECT COUNT(*) FROM course WHERE deleted = 0")
    Long countCourses();

    /**
     * 统计指定状态课程数量。
     * @param status 课程状态编码
     * @return 课程数量
     */
    @Select("SELECT COUNT(*) FROM course WHERE deleted = 0 AND status = #{status}")
    Long countCoursesByStatus(@Param("status") String status);

    /**
     * 统计课程章节数量。
     * @return 章节数量
     */
    @Select("SELECT COUNT(*) FROM course_chapter WHERE deleted = 0")
    Long countCourseChapters();

    /**
     * 统计课程小节数量。
     * @return 小节数量
     */
    @Select("SELECT COUNT(*) FROM course_section WHERE deleted = 0")
    Long countCourseSections();

    /**
     * 统计媒资文件数量。
     * @return 媒资文件数量
     */
    @Select("SELECT COUNT(*) FROM media_file WHERE deleted = 0")
    Long countMediaFiles();

    /**
     * 统计学习课程关系数量。
     * @return 学习课程关系数量
     */
    @Select("SELECT COUNT(*) FROM learning_course WHERE deleted = 0")
    Long countLearningCourses();

    /**
     * 统计学习记录数量。
     * @return 学习记录数量
     */
    @Select("SELECT COUNT(*) FROM learning_record WHERE deleted = 0")
    Long countLearningRecords();

    /**
     * 统计互动问题数量。
     * @return 问答问题数量
     */
    @Select("SELECT COUNT(*) FROM interaction_question WHERE deleted = 0")
    Long countInteractionQuestions();

    /**
     * 统计消息接收记录数量。
     * @return 消息接收记录数量
     */
    @Select("SELECT COUNT(*) FROM message_receiver WHERE deleted = 0")
    Long countMessageReceivers();

    /**
     * 统计知识库数量。
     * @return 知识库数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_base WHERE deleted = 0")
    Long countKnowledgeBases();

    /**
     * 统计知识库文档数量。
     * @return 文档数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_document WHERE deleted = 0")
    Long countKnowledgeDocuments();

    /**
     * 统计已解析文档数量。
     * @return 已解析文档数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_document WHERE deleted = 0 AND parse_status = 'SUCCESS'")
    Long countParsedDocuments();

    /**
     * 统计已完成向量化的文档数量。
     * @return 已向量化文档数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_document WHERE deleted = 0 AND embedding_status = 'SUCCESS'")
    Long countEmbeddedDocuments();

    /**
     * 统计已完成向量化的切片数量。
     * @return 已向量化切片数量
     */
    @Select("SELECT COUNT(*) FROM knowledge_segment WHERE deleted = 0 AND embedding_status = 'SUCCESS'")
    Long countEmbeddedSegments();

    /**
     * 统计 AI 会话数量。
     * @return AI 会话数量
     */
    @Select("SELECT COUNT(*) FROM ai_conversation WHERE deleted = 0")
    Long countAiConversations();

    /**
     * 统计 RAG 会话数量。
     * @return RAG 会话数量
     */
    @Select("SELECT COUNT(*) FROM ai_conversation WHERE deleted = 0 AND scene = 'RAG'")
    Long countRagConversations();

    /**
     * 统计 Tool Calling 调用日志数量。
     * @return Tool 调用日志数量
     */
    @Select("SELECT COUNT(*) FROM ai_tool_call_log")
    Long countToolCallLogs();

    /**
     * 统计试卷数量。
     * @return 试卷数量
     */
    @Select("SELECT COUNT(*) FROM exam_paper WHERE deleted = 0")
    Long countExamPapers();

    /**
     * 统计题目数量。
     * @return 题目数量
     */
    @Select("SELECT COUNT(*) FROM exam_question WHERE deleted = 0")
    Long countExamQuestions();

    /**
     * 统计订单数量。
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0")
    Long countOrders();

    /**
     * 统计已支付订单数量。
     * @return 已支付订单数量
     */
    @Select("SELECT COUNT(*) FROM trade_order WHERE deleted = 0 AND status = 'PAID'")
    Long countPaidOrders();

    /**
     * 统计事件发布日志数量。
     * @return 事件发布日志数量
     */
    @Select("SELECT COUNT(*) FROM event_publish_log WHERE deleted = 0")
    Long countEventPublishLogs();

    /**
     * 统计事件消费日志数量。
     * @return 事件消费日志数量
     */
    @Select("SELECT COUNT(*) FROM event_consume_log")
    Long countEventConsumeLogs();

    /**
     * 统计审计日志数量。
     * @return 审计日志数量
     */
    @Select("SELECT COUNT(*) FROM ops_audit_log")
    Long countAuditLogs();

    /**
     * 统计幂等记录数量。
     * @return 幂等记录数量
     */
    @Select("SELECT COUNT(*) FROM idempotent_record WHERE deleted = 0")
    Long countIdempotentRecords();

    /**
     * 统计后台日统计数量。
     * @return 后台日统计数量
     */
    @Select("SELECT COUNT(*) FROM admin_daily_stat WHERE deleted = 0")
    Long countAdminDailyStats();

    /**
     * 检查课程调试视图是否存在。
     * @return 视图数量
     */
    @Select("SELECT COUNT(*) FROM information_schema.VIEWS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'v_course_debug_overview'")
    Long countCourseDebugView();

    /**
     * 检查学习调试视图是否存在。
     * @return 视图数量
     */
    @Select("SELECT COUNT(*) FROM information_schema.VIEWS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'v_user_learning_overview'")
    Long countUserLearningView();
}
