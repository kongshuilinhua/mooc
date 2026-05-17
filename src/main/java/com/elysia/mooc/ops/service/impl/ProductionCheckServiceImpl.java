package com.elysia.mooc.ops.service.impl;

import com.elysia.mooc.ops.domain.enums.ProductionCheckStatus;
import com.elysia.mooc.ops.domain.vo.ProductionCheckItemVO;
import com.elysia.mooc.ops.domain.vo.ProductionCheckSummaryVO;
import com.elysia.mooc.ops.mapper.ProductionCheckMapper;
import com.elysia.mooc.ops.service.ProductionCheckService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 阶段一生产化巡检服务实现。 */
@Service
@RequiredArgsConstructor
public class ProductionCheckServiceImpl implements ProductionCheckService {

    private static final String GROUP_DATA = "演示数据";
    private static final String GROUP_AI = "AI 能力";
    private static final String GROUP_GOVERNANCE = "生产化治理";
    private static final String GROUP_ACCOUNT = "演示账号";

    private final ProductionCheckMapper productionCheckMapper;

    /**
     * 执行 day01-day23 阶段一巡检。
     * @return 巡检汇总结果
     */
    @Override
    public ProductionCheckSummaryVO checkStageOne() {
        List<ProductionCheckItemVO> items = new ArrayList<>();

        // 1. 数据基线用于快速发现运行库缺演示数据或 V024 调试视图未执行的问题。
        addAtLeast(items, "USER_COUNT", "用户数据", GROUP_DATA, productionCheckMapper.countUsers(), 3L);
        addAtLeast(items, "CATEGORY_COUNT", "课程分类", GROUP_DATA, productionCheckMapper.countCourseCategories(), 3L);
        addAtLeast(items, "TAG_COUNT", "课程标签", GROUP_DATA, productionCheckMapper.countCourseTags(), 1L);
        addAtLeast(items, "COURSE_COUNT", "课程总数", GROUP_DATA, productionCheckMapper.countCourses(), 6L);
        addAtLeast(items, "PUBLISHED_COURSE_COUNT", "已发布课程", GROUP_DATA, productionCheckMapper.countCoursesByStatus("PUBLISHED"), 1L);
        addWarnAtLeast(items, "DRAFT_COURSE_COUNT", "草稿课程", GROUP_DATA, productionCheckMapper.countCoursesByStatus("DRAFT"), 1L);
        addWarnAtLeast(items, "PENDING_COURSE_COUNT", "待审核课程", GROUP_DATA, productionCheckMapper.countCoursesByStatus("PENDING"), 1L);
        addWarnAtLeast(items, "REJECTED_COURSE_COUNT", "已驳回课程", GROUP_DATA, productionCheckMapper.countCoursesByStatus("REJECTED"), 1L);
        addWarnAtLeast(items, "OFFLINE_COURSE_COUNT", "已下架课程", GROUP_DATA, productionCheckMapper.countCoursesByStatus("OFFLINE"), 1L);
        addAtLeast(items, "CHAPTER_COUNT", "课程章节", GROUP_DATA, productionCheckMapper.countCourseChapters(), 12L);
        addAtLeast(items, "SECTION_COUNT", "课程小节", GROUP_DATA, productionCheckMapper.countCourseSections(), 24L);
        addWarnAtLeast(items, "MEDIA_FILE_COUNT", "媒资文件", GROUP_DATA, productionCheckMapper.countMediaFiles(), 1L);
        addWarnAtLeast(items, "LEARNING_COURSE_COUNT", "学习课程记录", GROUP_DATA, productionCheckMapper.countLearningCourses(), 1L);
        addWarnAtLeast(items, "LEARNING_RECORD_COUNT", "学习进度记录", GROUP_DATA, productionCheckMapper.countLearningRecords(), 1L);
        addWarnAtLeast(items, "INTERACTION_QUESTION_COUNT", "问答记录", GROUP_DATA, productionCheckMapper.countInteractionQuestions(), 1L);
        addWarnAtLeast(items, "MESSAGE_RECEIVER_COUNT", "消息接收记录", GROUP_DATA, productionCheckMapper.countMessageReceivers(), 1L);
        addAtLeast(items, "EXAM_PAPER_COUNT", "试卷", GROUP_DATA, productionCheckMapper.countExamPapers(), 1L);
        addAtLeast(items, "EXAM_QUESTION_COUNT", "题目", GROUP_DATA, productionCheckMapper.countExamQuestions(), 3L);
        addAtLeast(items, "ORDER_COUNT", "订单", GROUP_DATA, productionCheckMapper.countOrders(), 1L);
        addWarnAtLeast(items, "PAID_ORDER_COUNT", "已支付订单", GROUP_DATA, productionCheckMapper.countPaidOrders(), 1L);
        addWarnAtLeast(items, "ADMIN_DAILY_STAT_COUNT", "后台日统计", GROUP_DATA, productionCheckMapper.countAdminDailyStats(), 1L);
        addWarnAtLeast(items, "COURSE_DEBUG_VIEW", "课程调试视图", GROUP_DATA, productionCheckMapper.countCourseDebugView(), 1L);
        addWarnAtLeast(items, "USER_LEARNING_VIEW", "学习调试视图", GROUP_DATA, productionCheckMapper.countUserLearningView(), 1L);

        // 2. AI 链路涉及外部依赖，运行库未准备完整时先标警告，避免误判普通业务不可用。
        addAtLeast(items, "KNOWLEDGE_BASE_COUNT", "知识库", GROUP_AI, productionCheckMapper.countKnowledgeBases(), 1L);
        addAtLeast(items, "KNOWLEDGE_DOCUMENT_COUNT", "知识库文档", GROUP_AI, productionCheckMapper.countKnowledgeDocuments(), 1L);
        addWarnAtLeast(items, "PARSED_DOCUMENT_COUNT", "已解析文档", GROUP_AI, productionCheckMapper.countParsedDocuments(), 1L);
        addWarnAtLeast(items, "EMBEDDED_DOCUMENT_COUNT", "已向量化文档", GROUP_AI, productionCheckMapper.countEmbeddedDocuments(), 1L);
        addWarnAtLeast(items, "EMBEDDED_SEGMENT_COUNT", "已向量化切片", GROUP_AI, productionCheckMapper.countEmbeddedSegments(), 1L);
        addAtLeast(items, "AI_CONVERSATION_COUNT", "AI 会话", GROUP_AI, productionCheckMapper.countAiConversations(), 1L);
        addWarnAtLeast(items, "RAG_CONVERSATION_COUNT", "RAG 会话", GROUP_AI, productionCheckMapper.countRagConversations(), 1L);
        addWarnAtLeast(items, "TOOL_CALL_LOG_COUNT", "Tool 调用日志", GROUP_AI, productionCheckMapper.countToolCallLogs(), 1L);

        // 3. 生产化治理项对应 day11/day23 的事件、审计和幂等能力。
        addWarnAtLeast(items, "EVENT_PUBLISH_LOG_COUNT", "事件发布日志", GROUP_GOVERNANCE, productionCheckMapper.countEventPublishLogs(), 1L);
        addWarnAtLeast(items, "EVENT_CONSUME_LOG_COUNT", "事件消费日志", GROUP_GOVERNANCE, productionCheckMapper.countEventConsumeLogs(), 1L);
        addWarnAtLeast(items, "AUDIT_LOG_COUNT", "审计日志", GROUP_GOVERNANCE, productionCheckMapper.countAuditLogs(), 1L);
        addWarnAtLeast(items, "IDEMPOTENT_RECORD_COUNT", "幂等记录", GROUP_GOVERNANCE, productionCheckMapper.countIdempotentRecords(), 1L);

        // 4. 演示账号权限错位会直接影响联调结论，必须作为失败项处理。
        addAtLeast(items, "ADMIN_ROLE_BOUND", "管理员账号角色", GROUP_ACCOUNT, productionCheckMapper.countUserRole("admin", "ADMIN"), 1L);
        addAtLeast(items, "TEACHER_ROLE_BOUND", "教师账号角色", GROUP_ACCOUNT, productionCheckMapper.countUserRole("teacher", "TEACHER"), 1L);
        addAtLeast(items, "STUDENT_ROLE_BOUND", "学生账号角色", GROUP_ACCOUNT, productionCheckMapper.countUserRole("student", "STUDENT"), 1L);
        addExact(items, "TEACHER_ADMIN_ROLE", "教师账号不应有管理员角色", GROUP_ACCOUNT, productionCheckMapper.countUserRole("teacher", "ADMIN"), 0L);
        addExact(items, "STUDENT_ADMIN_ROLE", "学生账号不应有管理员角色", GROUP_ACCOUNT, productionCheckMapper.countUserRole("student", "ADMIN"), 0L);

        return buildSummary(items);
    }

    private void addAtLeast(
            List<ProductionCheckItemVO> items,
            String code,
            String name,
            String groupName,
            Long currentValue,
            Long expectedValue) {
        long current = safeLong(currentValue);
        ProductionCheckStatus status = current >= expectedValue ? ProductionCheckStatus.PASS : ProductionCheckStatus.FAILED;
        String message = status == ProductionCheckStatus.PASS
                ? "已满足阶段一联调要求"
                : "低于阶段一联调最低要求，请检查 SQL 或演示数据";
        items.add(new ProductionCheckItemVO(code, name, groupName, current, expectedValue, status, message));
    }

    private void addWarnAtLeast(
            List<ProductionCheckItemVO> items,
            String code,
            String name,
            String groupName,
            Long currentValue,
            Long expectedValue) {
        long current = safeLong(currentValue);
        ProductionCheckStatus status = current >= expectedValue ? ProductionCheckStatus.PASS : ProductionCheckStatus.WARN;
        String message = status == ProductionCheckStatus.PASS
                ? "已具备演示或排查数据"
                : "暂未发现数据，不阻塞启动，但会影响完整演示";
        items.add(new ProductionCheckItemVO(code, name, groupName, current, expectedValue, status, message));
    }

    private void addExact(
            List<ProductionCheckItemVO> items,
            String code,
            String name,
            String groupName,
            Long currentValue,
            Long expectedValue) {
        long current = safeLong(currentValue);
        ProductionCheckStatus status = current == expectedValue ? ProductionCheckStatus.PASS : ProductionCheckStatus.FAILED;
        String message = status == ProductionCheckStatus.PASS
                ? "演示账号权限边界正确"
                : "演示账号存在越权角色，请先修复 RBAC 种子或运行库数据";
        items.add(new ProductionCheckItemVO(code, name, groupName, current, expectedValue, status, message));
    }

    private ProductionCheckSummaryVO buildSummary(List<ProductionCheckItemVO> items) {
        int passCount = (int) items.stream().filter(item -> item.getStatus() == ProductionCheckStatus.PASS).count();
        int warnCount = (int) items.stream().filter(item -> item.getStatus() == ProductionCheckStatus.WARN).count();
        int failedCount = (int) items.stream().filter(item -> item.getStatus() == ProductionCheckStatus.FAILED).count();

        ProductionCheckSummaryVO summary = new ProductionCheckSummaryVO();
        summary.setStage("day01-day23 阶段一");
        summary.setCheckTime(LocalDateTime.now());
        summary.setTotalCount(items.size());
        summary.setPassCount(passCount);
        summary.setWarnCount(warnCount);
        summary.setFailedCount(failedCount);
        summary.setStatus(resolveSummaryStatus(warnCount, failedCount));
        summary.setMessage(resolveSummaryMessage(warnCount, failedCount));
        summary.setItems(items);
        return summary;
    }

    private ProductionCheckStatus resolveSummaryStatus(int warnCount, int failedCount) {
        if (failedCount > 0) {
            return ProductionCheckStatus.FAILED;
        }
        return warnCount > 0 ? ProductionCheckStatus.WARN : ProductionCheckStatus.PASS;
    }

    private String resolveSummaryMessage(int warnCount, int failedCount) {
        if (failedCount > 0) {
            return "阶段一存在阻塞项，请先按明细修复后再联调验收";
        }
        if (warnCount > 0) {
            return "阶段一核心链路可巡检，部分演示数据或外部依赖数据不足";
        }
        return "阶段一生产化巡检通过";
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }
}
