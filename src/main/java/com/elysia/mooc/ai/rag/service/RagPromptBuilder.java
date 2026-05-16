package com.elysia.mooc.ai.rag.service;

import com.elysia.mooc.ai.rag.service.impl.RetrievedSegment;
import java.util.List;

/** RAG Prompt 构造器。 */
public interface RagPromptBuilder {

    /**
     * 构造带引用约束的 Prompt。
     *
     * @param question 用户问题
     * @param segments 检索命中的知识切片
     * @return Prompt 文本
     */
    String build(String question, List<RetrievedSegment> segments);
}
