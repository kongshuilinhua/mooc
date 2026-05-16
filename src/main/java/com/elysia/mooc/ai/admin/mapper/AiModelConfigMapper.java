package com.elysia.mooc.ai.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.ai.admin.domain.po.AiModelConfigPO;
import org.apache.ibatis.annotations.Mapper;

/** AI 模型配置 Mapper。 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfigPO> {
}
