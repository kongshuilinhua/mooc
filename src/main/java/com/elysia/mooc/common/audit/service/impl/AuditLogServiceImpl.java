package com.elysia.mooc.common.audit.service.impl;

import com.elysia.mooc.common.audit.domain.po.OpsAuditLogPO;
import com.elysia.mooc.common.audit.mapper.OpsAuditLogMapper;
import com.elysia.mooc.common.audit.service.AuditLogService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 审计日志落库服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final OpsAuditLogMapper auditLogMapper;

    @Override
    public void saveQuietly(OpsAuditLogPO auditLog) {
        try {
            if (auditLog.getCreateTime() == null) {
                auditLog.setCreateTime(LocalDateTime.now());
            }
            auditLogMapper.insert(auditLog);
        } catch (Exception ex) {
            log.warn("审计日志写入失败，不影响主业务：{}", ex.getMessage());
        }
    }
}
