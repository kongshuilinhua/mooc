package com.elysia.mooc.common.audit.service;

import com.elysia.mooc.common.audit.domain.po.OpsAuditLogPO;

/** 审计日志落库服务。 */
public interface AuditLogService {

    /**
     * 保存审计日志。
     * 审计失败不能反向阻断主业务，因此实现层只记录告警日志。
     *
     * @param auditLog 审计日志实体
     */
    void saveQuietly(OpsAuditLogPO auditLog);
}
