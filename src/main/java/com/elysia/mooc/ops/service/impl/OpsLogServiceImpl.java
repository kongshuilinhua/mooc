package com.elysia.mooc.ops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.audit.domain.po.OpsAuditLogPO;
import com.elysia.mooc.common.audit.mapper.OpsAuditLogMapper;
import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import com.elysia.mooc.common.idempotent.mapper.IdempotentRecordMapper;
import com.elysia.mooc.ops.domain.dto.AuditLogQuery;
import com.elysia.mooc.ops.domain.dto.IdempotentRecordQuery;
import com.elysia.mooc.ops.domain.vo.AuditLogVO;
import com.elysia.mooc.ops.domain.vo.IdempotentRecordVO;
import com.elysia.mooc.ops.service.OpsLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 管理端运维治理查询服务实现。 */
@Service
@RequiredArgsConstructor
public class OpsLogServiceImpl implements OpsLogService {

    private final OpsAuditLogMapper auditLogMapper;
    private final IdempotentRecordMapper idempotentRecordMapper;

    @Override
    public PageResult<AuditLogVO> listAuditLogs(AuditLogQuery query) {
        AuditLogQuery safeQuery = query == null ? new AuditLogQuery() : query;
        LambdaQueryWrapper<OpsAuditLogPO> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(item -> item
                    .like(OpsAuditLogPO::getOperatorName, keyword)
                    .or().like(OpsAuditLogPO::getAction, keyword)
                    .or().like(OpsAuditLogPO::getTargetType, keyword)
                    .or().like(OpsAuditLogPO::getTargetId, keyword)
                    .or().like(OpsAuditLogPO::getRequestPath, keyword)
                    .or().like(OpsAuditLogPO::getTraceId, keyword));
        }
        wrapper.eq(StringUtils.hasText(safeQuery.getAction()), OpsAuditLogPO::getAction, trim(safeQuery.getAction()));
        wrapper.eq(safeQuery.getOperatorId() != null, OpsAuditLogPO::getOperatorId, safeQuery.getOperatorId());
        wrapper.eq(StringUtils.hasText(safeQuery.getTargetType()), OpsAuditLogPO::getTargetType, trim(safeQuery.getTargetType()));
        wrapper.eq(StringUtils.hasText(safeQuery.getTraceId()), OpsAuditLogPO::getTraceId, trim(safeQuery.getTraceId()));
        if (safeQuery.getSuccess() != null) {
            wrapper.eq(OpsAuditLogPO::getSuccess, Boolean.TRUE.equals(safeQuery.getSuccess()) ? 1 : 0);
        }
        wrapper.ge(safeQuery.getStartTime() != null, OpsAuditLogPO::getCreateTime, safeQuery.getStartTime());
        wrapper.le(safeQuery.getEndTime() != null, OpsAuditLogPO::getCreateTime, safeQuery.getEndTime());
        applyAuditSort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<OpsAuditLogPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<OpsAuditLogPO> result = auditLogMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toAuditLogVO);
    }

    @Override
    public PageResult<IdempotentRecordVO> listIdempotentRecords(IdempotentRecordQuery query) {
        IdempotentRecordQuery safeQuery = query == null ? new IdempotentRecordQuery() : query;
        LambdaQueryWrapper<IdempotentRecordPO> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(item -> item
                    .like(IdempotentRecordPO::getIdempotentKey, keyword)
                    .or().like(IdempotentRecordPO::getBizType, keyword)
                    .or().like(IdempotentRecordPO::getBizId, keyword)
                    .or().like(IdempotentRecordPO::getRequestHash, keyword));
        }
        wrapper.eq(StringUtils.hasText(safeQuery.getBizType()), IdempotentRecordPO::getBizType, trim(safeQuery.getBizType()));
        wrapper.eq(StringUtils.hasText(safeQuery.getBizId()), IdempotentRecordPO::getBizId, trim(safeQuery.getBizId()));
        wrapper.eq(safeQuery.getStatus() != null, IdempotentRecordPO::getStatus, safeQuery.getStatus());
        wrapper.ge(safeQuery.getStartTime() != null, IdempotentRecordPO::getCreateTime, safeQuery.getStartTime());
        wrapper.le(safeQuery.getEndTime() != null, IdempotentRecordPO::getCreateTime, safeQuery.getEndTime());
        applyIdempotentSort(wrapper, safeQuery.getSortBy(), safeQuery.getIsAsc());

        Page<IdempotentRecordPO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        Page<IdempotentRecordPO> result = idempotentRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toIdempotentRecordVO);
    }

    private void applyAuditSort(LambdaQueryWrapper<OpsAuditLogPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("costMs".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, OpsAuditLogPO::getCostMs);
        } else {
            wrapper.orderBy(true, asc, OpsAuditLogPO::getCreateTime);
        }
        wrapper.orderByDesc(OpsAuditLogPO::getId);
    }

    private void applyIdempotentSort(LambdaQueryWrapper<IdempotentRecordPO> wrapper, String sortBy, Boolean isAsc) {
        boolean asc = Boolean.TRUE.equals(isAsc);
        if ("expireTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, IdempotentRecordPO::getExpireTime);
        } else if ("updateTime".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, IdempotentRecordPO::getUpdateTime);
        } else {
            wrapper.orderBy(true, asc, IdempotentRecordPO::getCreateTime);
        }
        wrapper.orderByDesc(IdempotentRecordPO::getId);
    }

    private AuditLogVO toAuditLogVO(OpsAuditLogPO source) {
        AuditLogVO target = new AuditLogVO();
        target.setId(source.getId());
        target.setOperatorId(source.getOperatorId());
        target.setOperatorName(source.getOperatorName());
        target.setAction(source.getAction());
        target.setTargetType(source.getTargetType());
        target.setTargetId(source.getTargetId());
        target.setRequestMethod(source.getRequestMethod());
        target.setRequestPath(source.getRequestPath());
        target.setRequestIp(source.getRequestIp());
        target.setTraceId(source.getTraceId());
        target.setSuccess(source.getSuccess() != null && source.getSuccess() == 1);
        target.setErrorMessage(source.getErrorMessage());
        target.setCostMs(source.getCostMs());
        target.setCreateTime(source.getCreateTime());
        return target;
    }

    private IdempotentRecordVO toIdempotentRecordVO(IdempotentRecordPO source) {
        IdempotentRecordVO target = new IdempotentRecordVO();
        target.setId(source.getId());
        target.setIdempotentKey(source.getIdempotentKey());
        target.setBizType(source.getBizType());
        target.setBizId(source.getBizId());
        target.setRequestHash(source.getRequestHash());
        target.setResponseBody(source.getResponseBody());
        target.setStatus(source.getStatus());
        target.setStatusText(source.getStatus() == null ? null : source.getStatus().getDesc());
        target.setExpireTime(source.getExpireTime());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateTime(source.getUpdateTime());
        target.setCreateBy(source.getCreateBy());
        target.setUpdateBy(source.getUpdateBy());
        return target;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : value;
    }
}
