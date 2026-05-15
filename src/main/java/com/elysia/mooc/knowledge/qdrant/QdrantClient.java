package com.elysia.mooc.knowledge.qdrant;

import java.util.List;

/** Qdrant 客户端接口，封装 collection、写入、删除和检索细节。 */
public interface QdrantClient {

    /**
     * 确认 Collection 已存在，不存在时创建。
     */
    void ensureCollection();

    /**
     * 写入或覆盖单个向量点。
     *
     * @param pointId Qdrant point ID
     * @param vector 向量
     * @param payload 业务 payload
     * @return 实际写入的 point ID
     */
    String upsertPoint(String pointId, List<Float> vector, QdrantPointPayload payload);

    /**
     * 删除单个向量点。
     *
     * @param pointId Qdrant point ID
     */
    void deletePoint(String pointId);

    /**
     * 按向量检索相似切片。
     *
     * @param vector 查询向量
     * @param knowledgeBaseId 知识库过滤条件，可为空
     * @param topK 返回数量
     * @return 命中结果
     */
    List<VectorSearchResult> search(List<Float> vector, Long knowledgeBaseId, int topK);
}
