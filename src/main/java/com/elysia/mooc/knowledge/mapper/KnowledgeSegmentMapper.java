package com.elysia.mooc.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 知识切片 Mapper。 */
@Mapper
public interface KnowledgeSegmentMapper extends BaseMapper<KnowledgeSegmentPO> {

    /**
     * 物理删除指定文档的切片。
     *
     * @param documentId 文档 ID
     * @return 删除行数
     */
    @Delete("DELETE FROM knowledge_segment WHERE document_id = #{documentId}")
    int deleteByDocumentIdPhysically(@Param("documentId") Long documentId);
}
