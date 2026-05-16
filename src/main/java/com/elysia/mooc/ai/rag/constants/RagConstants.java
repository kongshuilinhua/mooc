package com.elysia.mooc.ai.rag.constants;

/** RAG 模块常量。 */
public final class RagConstants {

    /** 默认召回切片数量。 */
    public static final int DEFAULT_TOP_K = 5;

    /** 最大召回切片数量，避免 Prompt 过长。 */
    public static final int MAX_TOP_K = 20;

    /** 默认最小相似度，低于该分数的切片不进入 Prompt。 */
    public static final double DEFAULT_MIN_SCORE = 0.35D;

    /** 引用摘要最大长度。 */
    public static final int PREVIEW_LENGTH = 160;

    /** RAG 无命中固定回复。 */
    public static final String EMPTY_ANSWER = "暂未在知识库中找到相关内容";

    /** RAG 会话默认记忆策略。 */
    public static final String RAG_MEMORY_STRATEGY = "SUMMARY_FIRST";

    private RagConstants() {
    }
}
