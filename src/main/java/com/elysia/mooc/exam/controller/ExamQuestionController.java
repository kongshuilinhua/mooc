package com.elysia.mooc.exam.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.exam.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.exam.domain.dto.ExamQuestionQuery;
import com.elysia.mooc.exam.domain.vo.QuestionVO;
import com.elysia.mooc.exam.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 考试题库接口。 */
@Tag(name = "考试题库")
@Validated
@RestController
@RequestMapping("/api/exam/questions")
@RequiredArgsConstructor
public class ExamQuestionController {

    private final QuestionService questionService;

    /**
     * 分页查询题目。
     *
     * @param query 查询条件
     * @return 题目分页
     */
    @Operation(summary = "分页查询题目")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<QuestionVO>> listQuestions(@Valid ExamQuestionQuery query) {
        return ApiResult.ok(questionService.listQuestions(query));
    }

    /**
     * 创建题目。
     *
     * @param request 创建题目请求
     * @return 创建后的题目
     */
    @Operation(summary = "创建题目")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ParamChecker
    public ApiResult<QuestionVO> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        return ApiResult.ok(questionService.createQuestion(request));
    }
}
