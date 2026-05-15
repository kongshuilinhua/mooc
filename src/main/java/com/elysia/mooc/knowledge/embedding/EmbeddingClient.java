package com.elysia.mooc.knowledge.embedding;

/** Embedding 客户端接口，隔离具体模型供应商实现。 */
public interface EmbeddingClient {

    /**
     * 生成文本向量。
     *
     * @param request 向量化请求，包含文本、模型和维度
     * @return 向量化结果
     */
    EmbeddingResult embed(EmbeddingRequest request);
}
