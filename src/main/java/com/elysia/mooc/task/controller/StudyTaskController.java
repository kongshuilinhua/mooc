package com.elysia.mooc.task.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.task.domain.dto.CompleteStudyTaskRequest;
import com.elysia.mooc.task.domain.dto.CreateStudyTaskPlanRequest;
import com.elysia.mooc.task.domain.dto.DispatchStudyTaskReminderRequest;
import com.elysia.mooc.task.domain.vo.StudyTaskCompleteResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskPlanResultVO;
import com.elysia.mooc.task.domain.vo.StudyTaskReminderDispatchResultVO;
import com.elysia.mooc.task.service.StudyTaskService;
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

/** 学习任务与日历提醒接口。 */
@Tag(name = "学习任务与日历提醒")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study/tasks")
public class StudyTaskController {

    private final StudyTaskService studyTaskService;

    /**
     * 创建学习任务计划。
     *
     * @param request 创建计划请求
     * @return 创建结果
     */
    @Operation(summary = "创建学习任务计划")
    @PostMapping("/plans")
    @PreAuthorize("hasRole('STUDENT')")
    @ParamChecker
    public ApiResult<StudyTaskPlanResultVO> createTaskPlan(@Valid @RequestBody CreateStudyTaskPlanRequest request) {
        return ApiResult.ok(studyTaskService.createTaskPlan(request));
    }

    /**
     * 完成学习任务实例。
     *
     * @param instanceId 任务实例 ID
     * @param request 完成请求
     * @return 完成结果
     */
    @Operation(summary = "完成学习任务")
    @PostMapping("/instances/{instanceId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @ParamChecker
    public ApiResult<StudyTaskCompleteResultVO> completeTaskInstance(
            @PathVariable Long instanceId,
            @Valid @RequestBody(required = false) CompleteStudyTaskRequest request) {
        CompleteStudyTaskRequest safeRequest = request == null ? new CompleteStudyTaskRequest() : request;
        return ApiResult.ok(studyTaskService.completeTaskInstance(instanceId, safeRequest));
    }

    /**
     * 派发学习任务提醒。
     *
     * @param request 提醒派发请求
     * @return 派发结果
     */
    @Operation(summary = "派发学习任务提醒")
    @PostMapping("/reminders/dispatch")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('study:task:dispatch')")
    @ParamChecker
    public ApiResult<StudyTaskReminderDispatchResultVO> dispatchReminders(
            @Valid @RequestBody DispatchStudyTaskReminderRequest request) {
        return ApiResult.ok(studyTaskService.dispatchReminders(request));
    }
}
