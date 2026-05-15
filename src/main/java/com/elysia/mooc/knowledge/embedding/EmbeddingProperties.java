package com.elysia.mooc.knowledge.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Embedding 模型配置，避免模型名称、维度和密钥来源散落在业务代码中。 */
@Data
@Component
@ConfigurationProperties(prefix = "mooc.ai.embedding")
public class EmbeddingProperties {

    /** 模型供应商，当前默认使用百炼兼容 OpenAI 接口。 */
    private String provider = "bailian";

    /** OpenAI 兼容接口地址。 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /** API Key 所在环境变量名称。 */
    private String apiKeyEnv = "DASHSCOPE_API_KEY";

    /** 向量模型名称。 */
    private String model = "text-embedding-v4";

    /** 向量维度，必须与 Qdrant collection 的 vector size 一致。 */
    private int dimensions = 1024;

    /** 外部模型调用超时时间，单位秒。 */
    private int timeoutSeconds = 60;
}
