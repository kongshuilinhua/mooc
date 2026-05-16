package com.elysia.mooc.ai.rag.service.impl;

import com.elysia.mooc.ai.rag.constants.RagConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** RAG 问答配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "mooc.ai.rag")
public class RagProperties {

    /** 默认召回数量。 */
    private int defaultTopK = RagConstants.DEFAULT_TOP_K;

    /** 最大召回数量。 */
    private int maxTopK = RagConstants.MAX_TOP_K;

    /** 最小相似度，低于该值按无命中处理。 */
    private double minScore = RagConstants.DEFAULT_MIN_SCORE;

    /** Prompt 中允许放入的最大切片数量。 */
    private int maxPromptSegments = 5;

    /** 单个切片放入 Prompt 的最大字符数。 */
    private int maxSegmentChars = 800;
}
