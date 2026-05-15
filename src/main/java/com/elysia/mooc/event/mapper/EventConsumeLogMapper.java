package com.elysia.mooc.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.event.domain.po.EventConsumeLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 事件消费日志数据库访问接口。 */
@Mapper
public interface EventConsumeLogMapper extends BaseMapper<EventConsumeLogPO> {
}
