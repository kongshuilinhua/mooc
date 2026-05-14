package com.elysia.mooc.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.interaction.domain.po.InteractionReportPO;
import org.apache.ibatis.annotations.Mapper;

/** 互动举报表数据库访问接口。 */
@Mapper
public interface InteractionReportMapper extends BaseMapper<InteractionReportPO> {
}
