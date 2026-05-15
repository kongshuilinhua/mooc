package com.elysia.mooc.knowledge.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeSegmentQuery;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentParseStatusVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeSegmentVO;

/** 知识库文档解析与切片服务。 */
public interface KnowledgeDocumentParseService {

    /**
     * 手动触发文档解析。
     *
     * @param documentId 文档 ID
     * @return 解析后的文档信息
     */
    KnowledgeDocumentVO parseDocument(Long documentId);

    /**
     * 查询文档切片列表。
     *
     * @param documentId 文档 ID
     * @param query 查询条件
     * @return 切片分页
     */
    PageResult<KnowledgeSegmentVO> listSegments(Long documentId, KnowledgeSegmentQuery query);

    /**
     * 查询文档解析状态。
     *
     * @param documentId 文档 ID
     * @return 解析状态
     */
    KnowledgeDocumentParseStatusVO getParseStatus(Long documentId);
}
