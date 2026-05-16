package com.elysia.mooc.ai.rag.service;

import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.domain.vo.RagChatResult;
import com.elysia.mooc.ai.rag.domain.vo.RagSearchResult;

/** RAG 问答服务。 */
public interface RagService {

    /**
     * 执行非流式 RAG 问答。
     *
     * @param request RAG 问答请求
     * @return 问答结果和引用来源
     */
    RagChatResult chat(RagChatRequest request);

    /**
     * 预览 RAG 检索命中。
     *
     * @param request 检索请求
     * @return 检索命中列表
     */
    RagSearchResult search(RagSearchRequest request);
}
