package com.elysia.mooc.ai.rag.service;

import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.service.impl.RetrievedSegment;
import java.util.List;

/** 知识检索器，隔离 Embedding 和 Qdrant 细节。 */
public interface KnowledgeRetriever {

    /**
     * 按问题检索知识切片。
     *
     * @param request 检索请求
     * @return 已过滤的切片列表
     */
    List<RetrievedSegment> searchSegments(RagSearchRequest request);
}
