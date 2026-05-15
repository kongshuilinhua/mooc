package com.elysia.mooc.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.event.domain.po.EventPublishLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 事件发布日志数据库访问接口。 */
@Mapper
public interface EventPublishLogMapper extends BaseMapper<EventPublishLogPO> {
}
