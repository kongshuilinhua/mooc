package com.elysia.mooc.interaction.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.interaction.domain.dto.AcceptAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateAnswerRequest;
import com.elysia.mooc.interaction.domain.dto.CreateQuestionRequest;
import com.elysia.mooc.interaction.domain.dto.CreateRatingRequest;
import com.elysia.mooc.interaction.domain.dto.HandleReportRequest;
import com.elysia.mooc.interaction.domain.dto.LikeRequest;
import com.elysia.mooc.interaction.domain.dto.QuestionQuery;
import com.elysia.mooc.interaction.domain.dto.ReportQuery;
import com.elysia.mooc.interaction.domain.dto.ReportRequest;
import com.elysia.mooc.interaction.domain.vo.InteractionCreateResultVO;
import com.elysia.mooc.interaction.domain.vo.LikeResultVO;
import com.elysia.mooc.interaction.domain.vo.QuestionItemVO;
import com.elysia.mooc.interaction.domain.vo.RatingResultVO;
import com.elysia.mooc.interaction.domain.vo.ReportItemVO;
import com.elysia.mooc.interaction.domain.vo.ReportResultVO;
import com.elysia.mooc.interaction.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 课程互动控制器，提供问答、评价、收藏、点赞和举报接口。 */
@Tag(name = "课程互动")
@RestController
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    /**
     * 分页查询课程问题，匿名用户可访问。
     *
     * @param courseId 课程 ID
     * @param query    查询参数
     * @return 问题分页结果
     */
    @Operation(summary = "分页查询课程问题")
    @GetMapping("/api/courses/{courseId}/questions")
    public ApiResult<PageResult<QuestionItemVO>> listQuestions(
            @PathVariable Long courseId,
            @Valid QuestionQuery query) {
        return ApiResult.ok(interactionService.listQuestions(courseId, query));
    }

    /**
     * 创建课程问题。
     *
     * @param courseId 课程 ID
     * @param request  创建问题请求
     * @return 创建结果
     */
    @Operation(summary = "创建课程问题")
    @PostMapping("/api/courses/{courseId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<InteractionCreateResultVO> createQuestion(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateQuestionRequest request) {
        return ApiResult.ok(interactionService.createQuestion(courseId, request));
    }

    /**
     * 创建问题回答。
     *
     * @param questionId 问题 ID
     * @param request    创建回答请求
     * @return 创建结果
     */
    @Operation(summary = "创建问题回答")
    @PostMapping("/api/questions/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<InteractionCreateResultVO> createAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody CreateAnswerRequest request) {
        return ApiResult.ok(interactionService.createAnswer(questionId, request));
    }

    /**
     * 采纳回答，只允许提问者操作。
     *
     * @param answerId 回答 ID
     * @param request  采纳请求
     * @return 是否处理成功
     */
    @Operation(summary = "采纳回答")
    @PostMapping("/api/answers/{answerId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Boolean> acceptAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody(required = false) AcceptAnswerRequest request) {
        return ApiResult.ok(interactionService.acceptAnswer(answerId, request));
    }

    /**
     * 创建或更新课程评价。
     *
     * @param courseId 课程 ID
     * @param request  评分请求
     * @return 评价结果
     */
    @Operation(summary = "创建或更新课程评价")
    @PostMapping("/api/courses/{courseId}/ratings")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<RatingResultVO> rateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateRatingRequest request) {
        return ApiResult.ok(interactionService.rateCourse(courseId, request));
    }

    /**
     * 收藏课程，重复收藏按幂等成功处理。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    @Operation(summary = "收藏课程")
    @PostMapping("/api/courses/{courseId}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Boolean> favoriteCourse(@PathVariable Long courseId) {
        return ApiResult.ok(interactionService.favoriteCourse(courseId));
    }

    /**
     * 取消收藏课程，未收藏时按幂等成功处理。
     *
     * @param courseId 课程 ID
     * @return 是否处理成功
     */
    @Operation(summary = "取消收藏课程")
    @DeleteMapping("/api/courses/{courseId}/favorite")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Boolean> unfavoriteCourse(@PathVariable Long courseId) {
        return ApiResult.ok(interactionService.unfavoriteCourse(courseId));
    }

    /**
     * 点赞互动目标，重复点赞按幂等成功处理。
     *
     * @param request 点赞请求
     * @return 点赞结果
     */
    @Operation(summary = "点赞互动目标")
    @PostMapping("/api/interactions/likes")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<LikeResultVO> like(@Valid @RequestBody LikeRequest request) {
        return ApiResult.ok(interactionService.like(request));
    }

    /**
     * 举报互动目标，举报只生成工单，不直接删除内容。
     *
     * @param request 举报请求
     * @return 举报结果
     */
    @Operation(summary = "举报互动目标")
    @PostMapping("/api/interactions/reports")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<ReportResultVO> report(@Valid @RequestBody ReportRequest request) {
        return ApiResult.ok(interactionService.report(request));
    }

    /**
     * 管理端分页查询举报工单。
     *
     * @param query 查询参数
     * @return 举报分页
     */
    @Operation(summary = "管理端分页查询举报")
    @GetMapping("/api/admin/interactions/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<ReportItemVO>> listReports(@Valid ReportQuery query) {
        return ApiResult.ok(interactionService.listReports(query));
    }

    /**
     * 管理端处理举报工单。
     *
     * @param reportId 举报 ID
     * @param request  处理请求
     * @return 是否处理成功
     */
    @Operation(summary = "管理端处理举报")
    @PutMapping("/api/admin/interactions/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ParamChecker
    public ApiResult<Boolean> handleReport(
            @PathVariable Long reportId,
            @Valid @RequestBody HandleReportRequest request) {
        return ApiResult.ok(interactionService.handleReport(reportId, request));
    }
}
