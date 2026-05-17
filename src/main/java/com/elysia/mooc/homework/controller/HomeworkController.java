package com.elysia.mooc.homework.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.homework.domain.dto.GradeHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.PublishHomeworkRequest;
import com.elysia.mooc.homework.domain.dto.SubmitHomeworkRequest;
import com.elysia.mooc.homework.domain.vo.HomeworkAssignmentVO;
import com.elysia.mooc.homework.domain.vo.HomeworkGradeVO;
import com.elysia.mooc.homework.domain.vo.HomeworkSubmissionVO;
import com.elysia.mooc.homework.service.HomeworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 作业发布、提交与批改接口。 */
@Tag(name = "作业发布提交与批改")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/homework")
public class HomeworkController {

    private final HomeworkService homeworkService;

    /**
     * 教师发布作业。
     *
     * @param request 发布作业请求
     * @return 作业响应
     */
    @Operation(summary = "教师发布作业")
    @PostMapping("/assignments")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasAuthority('homework:manage')")
    @ParamChecker
    public ApiResult<HomeworkAssignmentVO> publishAssignment(@Valid @RequestBody PublishHomeworkRequest request) {
        return ApiResult.ok(homeworkService.publishAssignment(request));
    }

    /**
     * 学生提交作业。
     *
     * @param request 提交作业请求
     * @return 提交记录响应
     */
    @Operation(summary = "学生提交作业")
    @PostMapping("/submissions")
    @PreAuthorize("hasRole('STUDENT') or hasAuthority('homework:submit')")
    @ParamChecker
    public ApiResult<HomeworkSubmissionVO> submitHomework(@Valid @RequestBody SubmitHomeworkRequest request) {
        return ApiResult.ok(homeworkService.submitHomework(request));
    }

    /**
     * 教师批改作业。
     *
     * @param submissionId 提交记录 ID
     * @param request 批改请求
     * @return 批改结果响应
     */
    @Operation(summary = "教师批改作业")
    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasAuthority('homework:manage')")
    @ParamChecker
    public ApiResult<HomeworkGradeVO> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeHomeworkRequest request) {
        return ApiResult.ok(homeworkService.gradeSubmission(submissionId, request));
    }
}
