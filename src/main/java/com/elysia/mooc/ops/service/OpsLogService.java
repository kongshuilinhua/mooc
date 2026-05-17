package com.elysia.mooc.ops.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.ops.domain.dto.AuditLogQuery;
import com.elysia.mooc.ops.domain.dto.IdempotentRecordQuery;
import com.elysia.mooc.ops.domain.vo.AuditLogVO;
import com.elysia.mooc.ops.domain.vo.IdempotentRecordVO;

/** 管理端运维治理查询服务。 */
public interface OpsLogService {

    /**
     * 分页查询审计日志。
     *
     * @param query 查询条件
     * @return 审计日志分页
     */
    PageResult<AuditLogVO> listAuditLogs(AuditLogQuery query);

    /**
     * 分页查询幂等记录。
     *
     * @param query 查询条件
     * @return 幂等记录分页
     */
    PageResult<IdempotentRecordVO> listIdempotentRecords(IdempotentRecordQuery query);
}
