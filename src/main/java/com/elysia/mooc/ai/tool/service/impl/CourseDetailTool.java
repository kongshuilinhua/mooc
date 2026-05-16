package com.elysia.mooc.ai.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.ai.tool.constants.ToolCallErrorCode;
import com.elysia.mooc.ai.tool.domain.dto.CourseDetailArguments;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseChapterMapper;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 已发布课程详情和目录摘要工具。 */
@Component
@RequiredArgsConstructor
public class CourseDetailTool implements AiTool<CourseDetailArguments> {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper chapterMapper;
    private final CourseSectionMapper sectionMapper;

    @Override
    public String name() {
        return "CourseDetailTool";
    }

    @Override
    public Class<CourseDetailArguments> argumentType() {
        return CourseDetailArguments.class;
    }

    /**
     * 查询课程详情摘要。
     *
     * @param arguments 课程 ID 参数
     * @param loginUser 当前登录用户
     * @return 课程详情和目录摘要
     */
    @Override
    public Map<String, Object> execute(CourseDetailArguments arguments, LoginUser loginUser) {
        CoursePO course = courseMapper.selectById(arguments.getCourseId());
        if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BizException(ToolCallErrorCode.TOOL_PARAM_INVALID, "课程不存在或未发布");
        }
        List<CourseChapterPO> chapters = chapterMapper.selectPage(new Page<>(1, 5),
                new LambdaQueryWrapper<CourseChapterPO>()
                        .eq(CourseChapterPO::getCourseId, course.getId())
                        .orderByAsc(CourseChapterPO::getSort)
                        .orderByAsc(CourseChapterPO::getId)).getRecords();
        List<CourseSectionPO> sections = sectionMapper.selectPage(new Page<>(1, 10),
                new LambdaQueryWrapper<CourseSectionPO>()
                        .eq(CourseSectionPO::getCourseId, course.getId())
                        .eq(CourseSectionPO::getStatus, EnableStatus.ENABLED)
                        .orderByAsc(CourseSectionPO::getSort)
                        .orderByAsc(CourseSectionPO::getId)).getRecords();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", course.getId());
        result.put("title", course.getTitle());
        result.put("summary", course.getSummary());
        result.put("difficulty", course.getDifficulty());
        result.put("price", course.getPrice());
        result.put("learnCount", course.getLearnCount());
        result.put("chapterCount", chapters.size());
        result.put("sectionCount", sections.size());
        result.put("chapters", chapters.stream()
                .map(chapter -> Map.<String, Object>of("id", chapter.getId(), "title", chapter.getTitle()))
                .toList());
        result.put("sections", sections.stream()
                .map(section -> Map.<String, Object>of(
                        "id", section.getId(),
                        "chapterId", section.getChapterId(),
                        "title", section.getTitle(),
                        "durationSeconds", section.getDurationSeconds() == null ? 0 : section.getDurationSeconds()))
                .toList());
        return result;
    }

    @Override
    public String summarize(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return "课程详情为空。";
        }
        return "课程《" + result.get("title") + "》包含约 " + result.get("chapterCount")
                + " 个章节、" + result.get("sectionCount") + " 个可学习小节。";
    }
}
