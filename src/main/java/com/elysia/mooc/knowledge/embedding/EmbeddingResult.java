package com.elysia.mooc.knowledge.embedding;

import java.util.List;

/** Embedding 结果对象，保留模型和维度用于排查配置漂移。 */
public record EmbeddingResult(
        /** 模型返回的向量。 */
        List<Float> vector,
        /** 实际调用的模型名称。 */
        String model,
        /** 实际向量维度。 */
        int dimensions) {
}
