package com.elysia.mooc.ops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ops.domain.po.OpsConfigItemPO;
import org.apache.ibatis.annotations.Mapper;

/** 系统配置项数据访问接口。 */
@Mapper
public interface OpsConfigItemMapper extends BaseMapper<OpsConfigItemPO> {
}
