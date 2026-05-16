package com.elysia.mooc.ai.tool.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.ai.tool.constants.ToolCallErrorCode;
import com.elysia.mooc.ai.tool.domain.dto.LearningProgressArguments;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 当前用户课程学习进度工具。 */
@Component
@RequiredArgsConstructor
public class LearningProgressTool implements AiTool<LearningProgressArguments> {

    private final LearningCourseMapper learningCourseMapper;
    private final CourseMapper courseMapper;
    private final CourseSectionMapper sectionMapper;

    @Override
    public String name() {
        return "LearningProgressTool";
    }

    @Override
    public Class<LearningProgressArguments> argumentType() {
        return LearningProgressArguments.class;
    }

    /**
     * 查询当前登录用户某课程学习进度。
     *
     * @param arguments 课程 ID 参数
     * @param loginUser 当前登录用户
     * @return 学习进度摘要
     */
    @Override
    public Map<String, Object> execute(LearningProgressArguments arguments, LoginUser loginUser) {
        Long currentUserId = loginUser == null ? null : loginUser.getUserId();
        if (currentUserId == null) {
            throw new BizException(ToolCallErrorCode.TOOL_FORBIDDEN, "请先登录后再查询学习进度");
        }
        if (arguments.getUserId() != null && !arguments.getUserId().equals(currentUserId)) {
            throw new BizException(ToolCallErrorCode.TOOL_FORBIDDEN, "不允许查询他人的学习进度");
        }

        LearningCoursePO learningCourse = learningCourseMapper.selectOne(new LambdaQueryWrapper<LearningCoursePO>()
                .eq(LearningCoursePO::getUserId, currentUserId)
                .eq(LearningCoursePO::getCourseId, arguments.getCourseId()));
        if (learningCourse == null) {
            throw new BizException(ToolCallErrorCode.TOOL_PARAM_INVALID, "当前用户尚未学习该课程");
        }
        CoursePO course = courseMapper.selectById(arguments.getCourseId());
        CourseSectionPO section = learningCourse.getLastSectionId() == null
                ? null
                : sectionMapper.selectById(learningCourse.getLastSectionId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courseId", learningCourse.getCourseId());
        result.put("courseTitle", course == null ? null : course.getTitle());
        result.put("progressPercent", learningCourse.getProgressPercent());
        result.put("learnedSeconds", learningCourse.getLearnedSeconds());
        result.put("lastSectionId", learningCourse.getLastSectionId());
        result.put("lastSectionTitle", section == null ? null : section.getTitle());
        result.put("lastLearnTime", learningCourse.getLastLearnTime());
        result.put("finished", learningCourse.getFinished());
        return result;
    }

    @Override
    public String summarize(Map<String, Object> result) {
        return "课程《" + result.get("courseTitle") + "》当前学习进度为 "
                + result.get("progressPercent") + "%，最近学习小节："
                + (result.get("lastSectionTitle") == null ? "暂无" : result.get("lastSectionTitle")) + "。";
    }
}
