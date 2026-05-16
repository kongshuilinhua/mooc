package com.elysia.mooc.ai.tool.service.impl;

import com.elysia.mooc.ai.tool.domain.dto.ToolCallRequest;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallResult;
import com.elysia.mooc.ai.tool.service.ToolOrchestrationService;
import com.elysia.mooc.ai.tool.service.ToolRegistry;
import com.elysia.mooc.auth.security.LoginUser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 普通聊天 Tool 编排实现。 */
@Service
@RequiredArgsConstructor
public class ToolOrchestrationServiceImpl implements ToolOrchestrationService {

    private static final Pattern COURSE_ID_PATTERN = Pattern.compile("(?:courseId|课程ID|课程|course)[:：= ]*(\\d+)",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_TOOL_CALLS_PER_ROUND = 2;

    private final ToolRegistry toolRegistry;

    /**
     * 根据用户输入触发允许的只读工具。
     *
     * @param message        用户输入
     * @param conversationId 会话 ID
     * @param messageId      用户消息 ID
     * @param loginUser      当前登录用户
     * @return 本轮工具调用结果
     */
    @Override
    public List<ToolCallResult> planAndExecute(String message, Long conversationId, Long messageId, LoginUser loginUser) {
        List<ToolCallRequest> requests = plan(message, conversationId, messageId, loginUser);
        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream()
                .limit(MAX_TOOL_CALLS_PER_ROUND)
                .map(toolRegistry::dispatch)
                .toList();
    }

    /**
     * 生成写入模型上下文的工具摘要。
     *
     * @param results 工具调用结果
     * @return 中文上下文
     */
    @Override
    public String buildToolContext(List<ToolCallResult> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("以下是平台只读工具返回的事实摘要，请优先依据这些事实回答：\n");
        int index = 1;
        for (ToolCallResult result : results) {
            builder.append(index++)
                    .append(". 工具：")
                    .append(result.getToolName())
                    .append("；状态：")
                    .append(Boolean.TRUE.equals(result.getSuccess()) ? "成功" : "失败")
                    .append("；摘要：")
                    .append(result.getResultSummary());
            if (StringUtils.hasText(result.getErrorMessage())) {
                builder.append("；错误：").append(result.getErrorMessage());
            }
            builder.append('\n');
        }
        return builder.toString();
    }

    private List<ToolCallRequest> plan(String message, Long conversationId, Long messageId, LoginUser loginUser) {
        String text = message == null ? "" : message.trim();
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        List<ToolCallRequest> requests = new ArrayList<>();

        Long courseId = parseCourseId(text);
        if (shouldSearchCourse(text)) {
            requests.add(request("CourseSearchTool", courseSearchArguments(text), conversationId, messageId, loginUser));
        } else if (shouldQueryCourseDetail(text, courseId)) {
            requests.add(request("CourseDetailTool", Map.of("courseId", courseId), conversationId, messageId, loginUser));
        }

        if (shouldQueryRecentLearning(text)) {
            requests.add(request("RecentLearningTool", Map.of("limit", 5), conversationId, messageId, loginUser));
        } else if (shouldQueryLearningProgress(text, courseId)) {
            requests.add(request("LearningProgressTool", Map.of("courseId", courseId), conversationId, messageId, loginUser));
        }

        if (shouldSearchKnowledge(text)) {
            Map<String, Object> arguments = new LinkedHashMap<>();
            arguments.put("query", text);
            arguments.put("topK", 5);
            if (courseId != null) {
                arguments.put("courseId", courseId);
            }
            requests.add(request("KnowledgeSearchTool", arguments, conversationId, messageId, loginUser));
        }
        return requests;
    }

    private ToolCallRequest request(
            String toolName,
            Map<String, Object> arguments,
            Long conversationId,
            Long messageId,
            LoginUser loginUser) {
        return ToolCallRequest.builder()
                .toolName(toolName)
                .arguments(arguments)
                .conversationId(conversationId)
                .messageId(messageId)
                .loginUser(loginUser)
                .build();
    }

    private boolean shouldSearchCourse(String text) {
        return containsAny(text, "找", "搜索", "推荐", "有哪些", "有没有")
                && containsAny(text, "课程", "课", "Java", "Python", "AI", "前端", "后端", "入门");
    }

    private boolean shouldQueryCourseDetail(String text, Long courseId) {
        return courseId != null && containsAny(text, "详情", "目录", "章节", "小节", "介绍", "内容");
    }

    private boolean shouldQueryLearningProgress(String text, Long courseId) {
        return courseId != null && containsAny(text, "进度", "学到哪", "学习情况", "完成");
    }

    private boolean shouldQueryRecentLearning(String text) {
        return containsAny(text, "最近学习", "继续学习", "上次学", "学习记录");
    }

    private boolean shouldSearchKnowledge(String text) {
        return containsAny(text, "知识库", "资料", "文档", "切片", "RAG") && containsAny(text, "查", "检索", "搜索", "为什么", "怎么");
    }

    private Map<String, Object> courseSearchArguments(String text) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("keyword", extractCourseKeyword(text));
        arguments.put("limit", 5);
        return arguments;
    }

    private Long parseCourseId(String text) {
        Matcher matcher = COURSE_ID_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractCourseKeyword(String text) {
        String keyword = text
                .replaceAll("帮我|请|一下|找|搜索|推荐|有哪些|有没有|课程|课|学习|相关|适合|的", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return StringUtils.hasText(keyword) ? keyword : text;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
