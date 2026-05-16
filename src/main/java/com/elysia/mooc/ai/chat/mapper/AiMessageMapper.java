package com.elysia.mooc.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.chat.domain.po.AiMessagePO;
import org.apache.ibatis.annotations.Mapper;

/** AI 消息 Mapper。 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessagePO> {
}
