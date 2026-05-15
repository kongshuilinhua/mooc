package com.elysia.mooc.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import org.apache.ibatis.annotations.Mapper;

/** 知识切片 Mapper。 */
@Mapper
public interface KnowledgeSegmentMapper extends BaseMapper<KnowledgeSegmentPO> {
}
