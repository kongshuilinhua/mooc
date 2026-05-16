package com.elysia.mooc.ai.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.chat.domain.po.AiConversationPO;
import org.apache.ibatis.annotations.Mapper;

/** AI 会话 Mapper。 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversationPO> {
}
