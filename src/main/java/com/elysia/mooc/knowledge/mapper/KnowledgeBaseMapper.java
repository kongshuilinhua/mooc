package com.elysia.mooc.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import org.apache.ibatis.annotations.Mapper;

/** 知识库 Mapper。 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBasePO> {
}
