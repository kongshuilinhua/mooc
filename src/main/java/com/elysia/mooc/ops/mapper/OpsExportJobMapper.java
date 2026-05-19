package com.elysia.mooc.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ops.domain.po.OpsExportJobPO;
import org.apache.ibatis.annotations.Mapper;

/** 导出任务数据访问接口。 */
@Mapper
public interface OpsExportJobMapper extends BaseMapper<OpsExportJobPO> {
}
