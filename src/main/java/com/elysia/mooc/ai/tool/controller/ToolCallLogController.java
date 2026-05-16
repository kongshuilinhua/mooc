package com.elysia.mooc.ai.tool.controller;

import com.elysia.mooc.ai.tool.domain.dto.ToolCallLogQuery;
import com.elysia.mooc.ai.tool.domain.vo.ToolCallLogVO;
import com.elysia.mooc.ai.tool.service.ToolCallLogService;
import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端 Tool 调用日志接口。 */
@Tag(name = "AI Tool 调用日志")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai/tool-logs")
public class ToolCallLogController {

    private final ToolCallLogService toolCallLogService;

    /**
     * 分页查询 Tool 调用日志。
     *
     * @param query 查询条件
     * @return Tool 调用日志分页
     */
    @Operation(summary = "分页查询 Tool 调用日志")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:tool-log:view')")
    public ApiResult<PageResult<ToolCallLogVO>> listToolLogs(@Valid ToolCallLogQuery query) {
        return ApiResult.ok(toolCallLogService.listLogs(query));
    }
}
