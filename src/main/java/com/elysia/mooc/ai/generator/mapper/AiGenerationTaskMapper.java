package com.elysia.mooc.ai.generator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.generator.domain.po.AiGenerationTaskPO;
import org.apache.ibatis.annotations.Mapper;

/** AI 生成任务数据访问接口。 */
@Mapper
public interface AiGenerationTaskMapper extends BaseMapper<AiGenerationTaskPO> {
}
