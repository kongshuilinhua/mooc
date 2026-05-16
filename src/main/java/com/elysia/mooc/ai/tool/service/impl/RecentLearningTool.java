package com.elysia.mooc.ai.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.tool.constants.ToolCallErrorCode;
import com.elysia.mooc.ai.tool.domain.dto.RecentLearningArguments;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 当前用户最近学习记录工具。 */
@Component
@RequiredArgsConstructor
public class RecentLearningTool implements AiTool<RecentLearningArguments> {

    private final LearningCourseMapper learningCourseMapper;
    private final CourseMapper courseMapper;
    private final CourseSectionMapper sectionMapper;

    @Override
    public String name() {
        return "RecentLearningTool";
    }

    @Override
    public Class<RecentLearningArguments> argumentType() {
        return RecentLearningArguments.class;
    }

    /**
     * 查询当前用户最近学习课程。
     *
     * @param arguments 数量参数
     * @param loginUser 当前登录用户
     * @return 最近学习摘要
     */
    @Override
    public Map<String, Object> execute(RecentLearningArguments arguments, LoginUser loginUser) {
        Long currentUserId = loginUser == null ? null : loginUser.getUserId();
        if (currentUserId == null) {
            throw new BizException(ToolCallErrorCode.TOOL_FORBIDDEN, "请先登录后再查询最近学习");
        }
        int limit = arguments.getLimit() == null ? 5 : arguments.getLimit();
        Page<LearningCoursePO> page = learningCourseMapper.selectPage(new Page<>(1, limit),
                new LambdaQueryWrapper<LearningCoursePO>()
                        .eq(LearningCoursePO::getUserId, currentUserId)
                        .orderByDesc(LearningCoursePO::getLastLearnTime)
                        .orderByDesc(LearningCoursePO::getUpdateTime)
                        .orderByDesc(LearningCoursePO::getId));

        List<Long> courseIds = page.getRecords().stream().map(LearningCoursePO::getCourseId).distinct().toList();
        Map<Long, CoursePO> courseMap = courseIds.isEmpty()
                ? Collections.emptyMap()
                : courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(CoursePO::getId, Function.identity(), (left, right) -> left));
        List<Long> sectionIds = page.getRecords().stream()
                .map(LearningCoursePO::getLastSectionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        Map<Long, CourseSectionPO> sectionMap = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : sectionMapper.selectBatchIds(sectionIds).stream()
                        .collect(Collectors.toMap(CourseSectionPO::getId, Function.identity(), (left, right) -> left));

        List<Map<String, Object>> records = page.getRecords().stream()
                .map(item -> toLearningItem(item, courseMap.get(item.getCourseId()), sectionMap.get(item.getLastSectionId())))
                .toList();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        return result;
    }

    @Override
    public String summarize(Map<String, Object> result) {
        List<?> records = result == null ? List.of() : (List<?>) result.getOrDefault("records", List.of());
        if (records.isEmpty()) {
            return "当前用户还没有最近学习记录。";
        }
        return "最近学习 " + records.size() + " 门课程：" + records.stream()
                .map(item -> item instanceof Map<?, ?> map ? String.valueOf(map.get("courseTitle")) : String.valueOf(item))
                .limit(5)
                .reduce((left, right) -> left + "、" + right)
                .orElse("");
    }

    private Map<String, Object> toLearningItem(
            LearningCoursePO item,
            CoursePO course,
            CourseSectionPO section) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseId", item.getCourseId());
        result.put("courseTitle", course == null ? null : course.getTitle());
        result.put("progressPercent", item.getProgressPercent());
        result.put("learnedSeconds", item.getLearnedSeconds());
        result.put("lastSectionId", item.getLastSectionId());
        result.put("lastSectionTitle", section == null ? null : section.getTitle());
        result.put("lastLearnTime", item.getLastLearnTime());
        return result;
    }
}
