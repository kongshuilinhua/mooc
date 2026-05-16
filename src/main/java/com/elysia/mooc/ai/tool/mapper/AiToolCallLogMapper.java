package com.elysia.mooc.ai.tool.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.tool.domain.po.AiToolCallLogPO;
import org.apache.ibatis.annotations.Mapper;

/** Tool 调用日志数据库访问接口。 */
@Mapper
public interface AiToolCallLogMapper extends BaseMapper<AiToolCallLogPO> {
}
