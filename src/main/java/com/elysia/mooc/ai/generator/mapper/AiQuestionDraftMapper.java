package com.elysia.mooc.ai.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.generator.domain.po.AiQuestionDraftPO;
import org.apache.ibatis.annotations.Mapper;

/** AI 题目草稿数据访问接口。 */
@Mapper
public interface AiQuestionDraftMapper extends BaseMapper<AiQuestionDraftPO> {
}
