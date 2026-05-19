package com.elysia.mooc.ai.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.generator.domain.po.AiLearningPathPO;
import org.apache.ibatis.annotations.Mapper;

/** AI 学习路径数据访问接口。 */
@Mapper
public interface AiLearningPathMapper extends BaseMapper<AiLearningPathPO> {
}
