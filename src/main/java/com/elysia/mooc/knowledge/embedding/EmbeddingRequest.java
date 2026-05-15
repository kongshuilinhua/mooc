package com.elysia.mooc.knowledge.embedding;

/** Embedding 请求对象，当前只封装单段文本向量化。 */
public record EmbeddingRequest(
        /** 待向量化文本。 */
        String input,
        /** 模型名称。 */
        String model,
        /** 期望向量维度。 */
        int dimensions) {
}
