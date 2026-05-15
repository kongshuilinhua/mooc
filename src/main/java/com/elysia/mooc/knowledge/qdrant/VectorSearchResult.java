package com.elysia.mooc.knowledge.qdrant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Qdrant 检索命中结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorSearchResult {

    /** Qdrant point ID。 */
    private String vectorId;

    /** 相似度分数。 */
    private Double score;

    /** 命中 payload。 */
    private QdrantPointPayload payload;
}
