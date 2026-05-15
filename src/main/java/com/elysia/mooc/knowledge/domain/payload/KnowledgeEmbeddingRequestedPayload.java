package com.elysia.mooc.knowledge.domain.payload;

import java.time.LocalDateTime;

/** 知识库向量化请求事件载荷。 */
public record KnowledgeEmbeddingRequestedPayload(
        Long kbId,
        Long documentId,
        Long operatorId,
        String title,
        String sourceUrl,
        String contentHash,
        LocalDateTime requestedAt) {
}
