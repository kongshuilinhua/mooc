package com.elysia.mooc.exam.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.exam.domain.dto.CreatePaperRequest;
import com.elysia.mooc.exam.domain.dto.ExamPaperQuery;
import com.elysia.mooc.exam.domain.vo.PaperVO;
import com.elysia.mooc.exam.service.PaperService;
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

/** 考试试卷接口。 */
@Tag(name = "考试试卷")
@Validated
@RestController
@RequestMapping("/api/exam/papers")
@RequiredArgsConstructor
public class ExamPaperController {

    private final PaperService paperService;

    /**
     * 分页查询试卷。
     *
     * @param query 查询条件
     * @return 试卷分页
     */
    @Operation(summary = "分页查询试卷")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<PaperVO>> listPapers(@Valid ExamPaperQuery query) {
        return ApiResult.ok(paperService.listPapers(query));
    }

    /**
     * 创建试卷。
     *
     * @param request 创建试卷请求
     * @return 创建后的试卷
     */
    @Operation(summary = "创建试卷")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @ParamChecker
    public ApiResult<PaperVO> createPaper(@Valid @RequestBody CreatePaperRequest request) {
        return ApiResult.ok(paperService.createPaper(request));
    }
}
