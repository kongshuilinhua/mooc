package com.elysia.mooc.knowledge.qdrant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Qdrant 向量库配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "mooc.qdrant")
public class QdrantProperties {

    /** Qdrant HTTP 地址。 */
    private String url = "http://192.168.150.101:6333";

    /** 当前线上知识库 Collection。 */
    private String collection = "mooc_knowledge";

    /** Collection 向量维度。 */
    private int vectorSize = 1024;

    /** 向量距离函数。 */
    private String distance = "Cosine";

    /** 是否启动后尝试初始化 Collection。 */
    private boolean autoInitialize = true;

    /** Qdrant 调用超时时间，单位秒。 */
    private int timeoutSeconds = 10;
}
