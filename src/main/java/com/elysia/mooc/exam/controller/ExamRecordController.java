package com.elysia.mooc.exam.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.exam.domain.dto.SubmitExamRequest;
import com.elysia.mooc.exam.domain.vo.ExamRecordVO;
import com.elysia.mooc.exam.service.ExamRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 考试作答接口。 */
@Tag(name = "考试作答")
@Validated
@RestController
@RequestMapping("/api/exam/records")
@RequiredArgsConstructor
public class ExamRecordController {

    private final ExamRecordService examRecordService;

    /**
     * 提交试卷作答。
     *
     * @param request 提交作答请求
     * @return 作答结果
     */
    @Operation(summary = "提交试卷作答")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<ExamRecordVO> submit(@Valid @RequestBody SubmitExamRequest request) {
        return ApiResult.ok(examRecordService.submit(request));
    }
}
