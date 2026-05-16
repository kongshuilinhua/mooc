package com.elysia.mooc.ai.tool.service.impl;

import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.domain.vo.RagSearchResult;
import com.elysia.mooc.ai.rag.service.RagService;
import com.elysia.mooc.ai.tool.domain.dto.KnowledgeSearchArguments;
import com.elysia.mooc.ai.tool.service.AiTool;
import com.elysia.mooc.auth.security.LoginUser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 知识库检索工具，复用 day16 RAG 检索能力。 */
@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool implements AiTool<KnowledgeSearchArguments> {

    private final RagService ragService;

    @Override
    public String name() {
        return "KnowledgeSearchTool";
    }

    @Override
    public Class<KnowledgeSearchArguments> argumentType() {
        return KnowledgeSearchArguments.class;
    }

    /**
     * 执行知识库检索。
     *
     * @param arguments 检索参数
     * @param loginUser 当前登录用户，由 RagService 再次从上下文校验权限
     * @return 检索来源摘要
     */
    @Override
    public Map<String, Object> execute(KnowledgeSearchArguments arguments, LoginUser loginUser) {
        RagSearchRequest request = new RagSearchRequest();
        request.setQuery(arguments.getQuery());
        request.setKnowledgeBaseId(arguments.getKnowledgeBaseId());
        request.setCourseId(arguments.getCourseId());
        request.setTopK(arguments.getTopK());
        RagSearchResult searchResult = ragService.search(request);
        List<AiSourceVO> sources = searchResult.getSources() == null ? List.of() : searchResult.getSources();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", searchResult.getQuery());
        result.put("hitCount", sources.size());
        result.put("sources", sources.stream().map(this::toSourceItem).toList());
        return result;
    }

    @Override
    public String summarize(Map<String, Object> result) {
        int hitCount = result == null || result.get("hitCount") == null
                ? 0
                : Integer.parseInt(String.valueOf(result.get("hitCount")));
        if (hitCount <= 0) {
            return "知识库中暂未检索到相关内容。";
        }
        return "知识库检索命中 " + hitCount + " 条内容，可作为回答依据。";
    }

    private Map<String, Object> toSourceItem(AiSourceVO source) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("sourceType", source.getSourceType());
        item.put("kbId", source.getKbId());
        item.put("documentId", source.getDocumentId());
        item.put("segmentId", source.getSegmentId());
        item.put("courseId", source.getCourseId());
        item.put("title", source.getTitle());
        item.put("score", source.getScore());
        item.put("preview", source.getPreview());
        return item;
    }
}
