package com.elysia.mooc.common.idempotent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.common.idempotent.domain.po.IdempotentRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 幂等记录数据库访问接口。 */
@Mapper
public interface IdempotentRecordMapper extends BaseMapper<IdempotentRecordPO> {
}
