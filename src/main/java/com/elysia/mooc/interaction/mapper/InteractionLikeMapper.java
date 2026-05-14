package com.elysia.mooc.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.interaction.domain.po.InteractionLikePO;
import org.apache.ibatis.annotations.Mapper;

/** 互动点赞表数据库访问接口。 */
@Mapper
public interface InteractionLikeMapper extends BaseMapper<InteractionLikePO> {
}
