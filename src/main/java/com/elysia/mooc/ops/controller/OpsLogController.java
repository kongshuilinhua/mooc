package com.elysia.mooc.ops.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.ops.domain.dto.AuditLogQuery;
import com.elysia.mooc.ops.domain.dto.IdempotentRecordQuery;
import com.elysia.mooc.ops.domain.vo.AuditLogVO;
import com.elysia.mooc.ops.domain.vo.IdempotentRecordVO;
import com.elysia.mooc.ops.service.OpsLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端审计和幂等记录查询接口。 */
@Tag(name = "管理端-审计与幂等")
@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class OpsLogController {

    private final OpsLogService opsLogService;

    /**
     * 分页查询审计日志。
     *
     * @param query 查询条件
     * @return 审计日志分页
     */
    @Operation(summary = "分页查询审计日志")
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<AuditLogVO>> listAuditLogs(@Valid AuditLogQuery query) {
        return ApiResult.ok(opsLogService.listAuditLogs(query));
    }

    /**
     * 分页查询幂等记录。
     *
     * @param query 查询条件
     * @return 幂等记录分页
     */
    @Operation(summary = "分页查询幂等记录")
    @GetMapping("/idempotent-records")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<IdempotentRecordVO>> listIdempotentRecords(@Valid IdempotentRecordQuery query) {
        return ApiResult.ok(opsLogService.listIdempotentRecords(query));
    }
}
