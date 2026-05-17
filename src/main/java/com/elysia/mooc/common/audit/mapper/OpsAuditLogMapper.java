package com.elysia.mooc.common.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.common.audit.domain.po.OpsAuditLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 审计日志数据库访问接口。 */
@Mapper
public interface OpsAuditLogMapper extends BaseMapper<OpsAuditLogPO> {
}
