package com.elysia.mooc.knowledge.service;

import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;

/** 知识库向量化服务。 */
public interface EmbeddingService {

    /**
     * 重建单个切片向量。
     *
     * @param segmentId 切片 ID
     * @return true 表示处理成功
     */
    Boolean rebuildSegment(Long segmentId);

    /**
     * 重建文档下全部切片向量。
     *
     * @param documentId 文档 ID
     * @return true 表示处理成功
     */
    Boolean rebuildDocument(Long documentId);

    /**
     * 消费向量化请求事件。
     *
     * @param payload 向量化请求载荷
     */
    void handleEmbeddingRequested(KnowledgeEmbeddingRequestedPayload payload);
}
