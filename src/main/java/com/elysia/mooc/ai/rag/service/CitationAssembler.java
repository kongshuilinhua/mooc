package com.elysia.mooc.ai.rag.service;

import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.rag.service.impl.RetrievedSegment;
import java.util.List;

/** 引用来源组装器。 */
public interface CitationAssembler {

    /**
     * 将命中切片转换为前端展示和落库共用的引用结构。
     *
     * @param segments 命中切片
     * @return 引用来源
     */
    List<AiSourceVO> buildCitations(List<RetrievedSegment> segments);

    /**
     * 将引用来源序列化为数据库 JSON。
     *
     * @param citations 引用来源
     * @return JSON 字符串
     */
    String toJson(List<AiSourceVO> citations);
}
