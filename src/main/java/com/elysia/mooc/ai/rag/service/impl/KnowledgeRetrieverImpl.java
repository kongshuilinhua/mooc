package com.elysia.mooc.ai.rag.service.impl;

import com.elysia.mooc.ai.rag.constants.RagErrorCode;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.service.KnowledgeRetriever;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.knowledge.embedding.EmbeddingClient;
import com.elysia.mooc.knowledge.embedding.EmbeddingProperties;
import com.elysia.mooc.knowledge.embedding.EmbeddingRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingResult;
import com.elysia.mooc.knowledge.qdrant.QdrantClient;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.qdrant.QdrantProperties;
import com.elysia.mooc.knowledge.qdrant.VectorSearchResult;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 基于 Embedding 和 Qdrant 的知识检索器。 */
@Service
@RequiredArgsConstructor
public class KnowledgeRetrieverImpl implements KnowledgeRetriever {

    private final EmbeddingProperties embeddingProperties;
    private final QdrantProperties qdrantProperties;
    private final RagProperties ragProperties;
    private final EmbeddingClient embeddingClient;
    private final QdrantClient qdrantClient;

    /**
     * 检索可进入 RAG Prompt 的知识切片。
     *
     * @param request 检索请求
     * @return 过滤后的切片
     */
    @Override
    public List<RetrievedSegment> searchSegments(RagSearchRequest request) {
        RagSearchRequest safeRequest = requireRequest(request);
        try {
            EmbeddingResult embedding = embeddingClient.embed(new EmbeddingRequest(
                    safeRequest.getQuery().trim(),
                    embeddingProperties.getModel(),
                    embeddingProperties.getDimensions()));
            validateVectorDimensions(embedding);
            int topK = resolveTopK(safeRequest.getTopK());
            return qdrantClient.search(embedding.vector(), safeRequest.getKnowledgeBaseId(), topK)
                    .stream()
                    .map(this::toSegment)
                    .filter(segment -> segment != null && segment.score() != null)
                    .filter(segment -> segment.score() >= ragProperties.getMinScore())
                    .filter(segment -> sameCourse(segment, safeRequest.getCourseId()))
                    .collect(() -> new LinkedHashMap<String, RetrievedSegment>(),
                            this::putIfAbsent,
                            Map::putAll)
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(RetrievedSegment::score).reversed())
                    .limit(ragProperties.getMaxPromptSegments())
                    .toList();
        } catch (BizException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BizException(RagErrorCode.RAG_RETRIEVAL_FAILED, "知识库检索服务暂不可用，请稍后重试");
        }
    }

    private RagSearchRequest requireRequest(RagSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            throw new BizException(RagErrorCode.RAG_PARAM_INVALID, "检索文本不能为空");
        }
        request.setTopK(resolveTopK(request.getTopK()));
        return request;
    }

    private int resolveTopK(Integer topK) {
        int defaultTopK = Math.max(1, ragProperties.getDefaultTopK());
        int maxTopK = Math.max(defaultTopK, ragProperties.getMaxTopK());
        if (topK == null) {
            return defaultTopK;
        }
        return Math.min(Math.max(1, topK), maxTopK);
    }

    private void validateVectorDimensions(EmbeddingResult result) {
        if (result == null || result.vector() == null || result.vector().isEmpty()) {
            throw new BizException(RagErrorCode.RAG_RETRIEVAL_FAILED, "向量模型返回结果为空");
        }
        if (result.dimensions() != embeddingProperties.getDimensions()
                || result.vector().size() != qdrantProperties.getVectorSize()
                || embeddingProperties.getDimensions() != qdrantProperties.getVectorSize()) {
            throw new BizException(RagErrorCode.RAG_RETRIEVAL_FAILED, "向量维度与 Qdrant Collection 配置不一致，必须重建索引");
        }
    }

    private RetrievedSegment toSegment(VectorSearchResult result) {
        if (result == null || result.getPayload() == null) {
            return null;
        }
        QdrantPointPayload payload = result.getPayload();
        return new RetrievedSegment(
                payload.getKbId(),
                payload.getDocumentId(),
                payload.getSegmentId(),
                payload.getCourseId(),
                payload.getSourceType(),
                payload.getTitle(),
                payload.getContent(),
                result.getScore(),
                preview(payload.getContent()));
    }

    private boolean sameCourse(RetrievedSegment segment, Long courseId) {
        return courseId == null || courseId.equals(segment.courseId());
    }

    private void putIfAbsent(Map<String, RetrievedSegment> target, RetrievedSegment segment) {
        String key = segment.kbId() + ":" + segment.documentId() + ":" + segment.segmentId();
        target.putIfAbsent(key, segment);
    }

    private String preview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String text = content.trim().replaceAll("\\s+", " ");
        int length = Math.min(text.length(), 160);
        return text.substring(0, length);
    }
}
