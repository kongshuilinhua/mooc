package com.elysia.mooc.learning.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.idempotent.Idempotent;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.learning.domain.dto.JoinCourseRequest;
import com.elysia.mooc.learning.domain.dto.LearningCourseQuery;
import com.elysia.mooc.learning.domain.dto.LearningHeartbeatRequest;
import com.elysia.mooc.learning.domain.dto.LearningHistoryQuery;
import com.elysia.mooc.learning.domain.vo.LearningCourseItem;
import com.elysia.mooc.learning.domain.vo.LearningHistoryItem;
import com.elysia.mooc.learning.domain.vo.LearningRecordVO;
import com.elysia.mooc.learning.domain.vo.LearningStatisticsVO;
import com.elysia.mooc.learning.service.LearningService;
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

/** 学习计划与学习进度接口。 */
@Tag(name = "学习计划与学习进度")
@Validated
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    /**
     * 加入课程。
     *
     * @param request 加入课程请求
     * @return 是否加入成功
     */
    @Operation(summary = "加入课程")
    @PostMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    @Idempotent(bizType = "LEARNING_JOIN", bizId = "#request.courseId")
    public ApiResult<Boolean> joinCourse(@Valid @RequestBody JoinCourseRequest request) {
        return ApiResult.ok(learningService.joinCourse(request));
    }

    /**
     * 查询我的课程。
     *
     * @param query 查询参数
     * @return 我的课程分页
     */
    @Operation(summary = "查询我的课程")
    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<LearningCourseItem>> listMyCourses(@Valid LearningCourseQuery query) {
        return ApiResult.ok(learningService.listMyCourses(query));
    }

    /**
     * 上报学习心跳。
     *
     * @param request 心跳请求
     * @return 更新后的学习记录
     */
    @Operation(summary = "上报学习心跳")
    @PostMapping("/records/heartbeat")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    public ApiResult<LearningRecordVO> heartbeat(@Valid @RequestBody LearningHeartbeatRequest request) {
        return ApiResult.ok(learningService.heartbeat(request));
    }

    /**
     * 查询学习历史。
     *
     * @param query 查询参数
     * @return 学习历史分页
     */
    @Operation(summary = "查询学习历史")
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<LearningHistoryItem>> listHistory(@Valid LearningHistoryQuery query) {
        return ApiResult.ok(learningService.listHistory(query));
    }

    /**
     * 查询学习统计。
     *
     * @return 学习统计
     */
    @Operation(summary = "查询学习统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<LearningStatisticsVO> getStatistics() {
        return ApiResult.ok(learningService.getStatistics());
    }
}
