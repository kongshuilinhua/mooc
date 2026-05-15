package com.elysia.mooc.knowledge.service;

import com.elysia.mooc.knowledge.domain.dto.VectorSearchRequest;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchResponseVO;

/** 知识库向量检索服务。 */
public interface VectorSearchService {

    /**
     * 执行向量检索调试。
     *
     * @param request 检索请求
     * @return 命中来源
     */
    VectorSearchResponseVO search(VectorSearchRequest request);
}
