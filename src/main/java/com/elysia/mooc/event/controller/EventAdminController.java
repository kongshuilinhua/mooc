package com.elysia.mooc.event.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.event.domain.dto.EventConsumeLogQuery;
import com.elysia.mooc.event.domain.dto.EventPublishLogQuery;
import com.elysia.mooc.event.domain.vo.EventConsumeLogVO;
import com.elysia.mooc.event.domain.vo.EventPublishLogVO;
import com.elysia.mooc.event.service.EventAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端事件日志接口。 */
@Tag(name = "事件基础设施")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
public class EventAdminController {

    private final EventAdminService eventAdminService;

    /**
     * 分页查询事件发布日志。
     *
     * @param query 查询条件
     * @return 发布日志分页
     */
    @Operation(summary = "分页查询事件发布日志")
    @GetMapping("/publish-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<EventPublishLogVO>> listPublishLogs(@Valid EventPublishLogQuery query) {
        return ApiResult.ok(eventAdminService.listPublishLogs(query));
    }

    /**
     * 分页查询事件消费日志。
     *
     * @param query 查询条件
     * @return 消费日志分页
     */
    @Operation(summary = "分页查询事件消费日志")
    @GetMapping("/consume-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<EventConsumeLogVO>> listConsumeLogs(@Valid EventConsumeLogQuery query) {
        return ApiResult.ok(eventAdminService.listConsumeLogs(query));
    }

    /**
     * 手动重试事件。
     *
     * @param eventId 全局唯一事件 ID，按字符串处理
     * @return 是否重试成功
     */
    @Operation(summary = "手动重试事件")
    @PostMapping("/{eventId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Boolean> retry(@PathVariable String eventId) {
        return ApiResult.ok(eventAdminService.retry(eventId));
    }
}
