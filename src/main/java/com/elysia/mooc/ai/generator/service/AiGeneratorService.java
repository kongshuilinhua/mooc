package com.elysia.mooc.ai.generator.service;

import com.elysia.mooc.ai.generator.domain.dto.GenerateChapterSummaryRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateLearningPathRequest;
import com.elysia.mooc.ai.generator.domain.dto.GenerateQuestionsRequest;
import com.elysia.mooc.ai.generator.domain.vo.ChapterSummaryResultVO;
import com.elysia.mooc.ai.generator.domain.vo.GeneratedQuestionsResultVO;
import com.elysia.mooc.ai.generator.domain.vo.LearningPathResultVO;

/** AI 生成服务。 */
public interface AiGeneratorService {

    /**
     * 生成章节总结。
     *
     * @param chapterId 章节 ID
     * @param request 总结生成参数
     * @return 章节总结结果
     */
    ChapterSummaryResultVO generateChapterSummary(Long chapterId, GenerateChapterSummaryRequest request);

    /**
     * 生成课程练习题草稿。
     *
     * @param courseId 课程 ID
     * @param request 出题参数
     * @return 题目草稿生成结果
     */
    GeneratedQuestionsResultVO generateQuestionDrafts(Long courseId, GenerateQuestionsRequest request);

    /**
     * 生成学生学习路径。
     *
     * @param studentId 学生 ID
     * @param request 学习路径参数
     * @return 学习路径生成结果
     */
    LearningPathResultVO generateLearningPath(Long studentId, GenerateLearningPathRequest request);
}
