package com.elysia.mooc.exam.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.exam.domain.dto.WrongQuestionQuery;
import com.elysia.mooc.exam.domain.vo.WrongQuestionVO;
import com.elysia.mooc.exam.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 考试错题本接口。 */
@Tag(name = "考试错题本")
@Validated
@RestController
@RequestMapping("/api/exam/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    /**
     * 分页查询当前用户错题。
     *
     * @param query 查询条件
     * @return 错题分页
     */
    @Operation(summary = "分页查询当前用户错题")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<WrongQuestionVO>> listWrongQuestions(@Valid WrongQuestionQuery query) {
        return ApiResult.ok(wrongQuestionService.listWrongQuestions(query));
    }
}
