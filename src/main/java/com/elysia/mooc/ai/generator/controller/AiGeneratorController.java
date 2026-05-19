package com.elysia.mooc.ai.generator.controller;

import com.elysia.mooc.ai.generator.domain.dto.GenerateChapterSummaryRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateLearningPathRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateQuestionsRequest;
import com.elysia.mooc.ai.generator.domain.vo.ChapterSummaryResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionsResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathResultVO;
import com.elysia.mooc.ai.generator.service.AiGeneratorService;
import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.validate.ParamChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 出题、总结和学习路径生成接口。 */
@RestController
@RequestMapping("/api/ai/generator")
@RequiredArgsConstructor
public class AiGeneratorController {

    private final AiGeneratorService aiGeneratorService;

    /**
     * 生成章节总结。
     * @param chapterId 章节 ID
     * @param request 生成参数
     * @return 章节总结结果
     */
    @PostMapping("/chapters/{chapterId}/summary")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ApiResult<ChapterSummaryResultVO> generateChapterSummary(
            @PathVariable Long chapterId,
            @Valid @RequestBody(required = false) GenerateChapterSummaryRequest request) {
        return ApiResult.ok(aiGeneratorService.generateChapterSummary(chapterId, request));
    }

    /**
     * 生成课程练习题草稿。
     * @param courseId 课程 ID
     * @param request 出题参数
     * @return 题目草稿生成结果
     */
    @PostMapping("/courses/{courseId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    @ParamChecker
    public ApiResult<GeneratedQuestionsResultVO> generateQuestions(
            @PathVariable Long courseId,
            @Valid @RequestBody GenerateQuestionsRequest request) {
        return ApiResult.ok(aiGeneratorService.generateQuestionDrafts(courseId, request));
    }

    /**
     * 生成学习路径。
     * @param studentId 学生 ID
     * @param request 生成参数
     * @return 学习路径结果
     */
    @PostMapping("/students/{studentId}/learning-path")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @ParamChecker
    public ApiResult<LearningPathResultVO> generateLearningPath(
            @PathVariable Long studentId,
            @Valid @RequestBody GenerateLearningPathRequest request) {
        return ApiResult.ok(aiGeneratorService.generateLearningPath(studentId, request));
    }
}
