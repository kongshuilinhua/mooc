package com.elysia.mooc.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import org.apache.ibatis.annotations.Mapper;

/** 知识库文档 Mapper。 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocumentPO> {
}
