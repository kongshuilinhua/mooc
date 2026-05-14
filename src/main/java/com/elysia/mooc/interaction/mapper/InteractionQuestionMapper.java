package com.elysia.mooc.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.interaction.domain.po.InteractionQuestionPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程问答问题表数据库访问接口。 */
@Mapper
public interface InteractionQuestionMapper extends BaseMapper<InteractionQuestionPO> {
}
