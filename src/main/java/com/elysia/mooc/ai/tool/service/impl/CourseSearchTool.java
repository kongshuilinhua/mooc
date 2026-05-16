package com.elysia.mooc.ai.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.tool.constants.ToolCallConstants;
import com.elysia.mooc.ai.tool.domain.dto.CourseSearchArguments;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 已发布课程搜索工具。 */
@Component
@RequiredArgsConstructor
public class CourseSearchTool implements AiTool<CourseSearchArguments> {

    private final CourseMapper courseMapper;

    @Override
    public String name() {
        return "CourseSearchTool";
    }

    @Override
    public Class<CourseSearchArguments> argumentType() {
        return CourseSearchArguments.class;
    }

    /**
     * 搜索已发布课程。
     *
     * @param arguments 搜索参数
     * @param loginUser 当前登录用户
     * @return 课程摘要
     */
    @Override
    public Map<String, Object> execute(CourseSearchArguments arguments, LoginUser loginUser) {
        int limit = arguments.getLimit() == null ? ToolCallConstants.DEFAULT_COURSE_LIMIT : arguments.getLimit();
        LambdaQueryWrapper<CoursePO> wrapper = new LambdaQueryWrapper<CoursePO>()
                .eq(CoursePO::getStatus, CourseStatus.PUBLISHED);
        if (StringUtils.hasText(arguments.getKeyword())) {
            String keyword = arguments.getKeyword().trim();
            wrapper.and(nested -> nested.like(CoursePO::getTitle, keyword)
                    .or()
                    .like(CoursePO::getSummary, keyword));
        }
        wrapper.orderByDesc(CoursePO::getRatingScore)
                .orderByDesc(CoursePO::getLearnCount)
                .orderByDesc(CoursePO::getPublishTime)
                .orderByDesc(CoursePO::getId);
        Page<CoursePO> page = courseMapper.selectPage(new Page<>(1, limit), wrapper);
        List<Map<String, Object>> courses = page.getRecords().stream().map(this::toCourseItem).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", StringUtils.hasText(arguments.getKeyword()) ? arguments.getKeyword().trim() : "");
        result.put("total", page.getTotal());
        result.put("courses", courses);
        return result;
    }

    @Override
    public String summarize(Map<String, Object> result) {
        List<?> courses = result == null ? List.of() : (List<?>) result.getOrDefault("courses", List.of());
        if (courses.isEmpty()) {
            return "没有找到匹配的已发布课程。";
        }
        return "找到 " + courses.size() + " 门已发布课程：" + courses.stream()
                .map(item -> item instanceof Map<?, ?> map ? String.valueOf(map.get("title")) : String.valueOf(item))
                .limit(5)
                .reduce((left, right) -> left + "、" + right)
                .orElse("");
    }

    private Map<String, Object> toCourseItem(CoursePO course) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", course.getId());
        item.put("title", course.getTitle());
        item.put("summary", course.getSummary());
        item.put("difficulty", course.getDifficulty());
        item.put("price", course.getPrice());
        item.put("learnCount", course.getLearnCount());
        item.put("ratingScore", course.getRatingScore());
        return item;
    }
}
