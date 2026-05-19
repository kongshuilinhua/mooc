package com.elysia.mooc.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ops.domain.po.OpsReviewTaskPO;
import org.apache.ibatis.annotations.Mapper;

/** 审核任务数据访问接口。 */
@Mapper
public interface OpsReviewTaskMapper extends BaseMapper<OpsReviewTaskPO> {
}
