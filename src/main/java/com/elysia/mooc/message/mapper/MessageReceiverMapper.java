package com.elysia.mooc.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.message.domain.po.MessageReceiverPO;
import org.apache.ibatis.annotations.Mapper;

/** 消息接收记录数据库访问接口。 */
@Mapper
public interface MessageReceiverMapper extends BaseMapper<MessageReceiverPO> {
}
