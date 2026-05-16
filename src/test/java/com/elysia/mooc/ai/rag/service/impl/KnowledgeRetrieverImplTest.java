package com.elysia.mooc.ai.rag.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingClient;
import com.elysia.mooc.knowledge.embedding.EmbeddingProperties;
import com.elysia.mooc.knowledge.embedding.EmbeddingRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingResult;
import com.elysia.mooc.knowledge.qdrant.QdrantClient;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.qdrant.QdrantProperties;
import com.elysia.mooc.knowledge.qdrant.VectorSearchResult;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** RAG 知识检索器测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeRetrieverImplTest {

    @Mock
    private EmbeddingClient embeddingClient;

    @Mock
    private QdrantClient qdrantClient;

    private KnowledgeRetrieverImpl retriever;
    private RagProperties ragProperties;

    @BeforeEach
    void setUp() {
        EmbeddingProperties embeddingProperties = new EmbeddingProperties();
        QdrantProperties qdrantProperties = new QdrantProperties();
        ragProperties = new RagProperties();
        ragProperties.setMinScore(0.5D);
        retriever = new KnowledgeRetrieverImpl(
                embeddingProperties,
                qdrantProperties,
                ragProperties,
                embeddingClient,
                qdrantClient);
    }

    @Test
    void searchSegmentsShouldFilterLowScoreAndDifferentCourse() {
        when(embeddingClient.embed(any(EmbeddingRequest.class)))
                .thenReturn(new EmbeddingResult(Collections.nCopies(1024, 0.1F), "text-embedding-v4", 1024));
        when(qdrantClient.search(any(), eq(12002L), eq(5))).thenReturn(List.of(
                hit(12204L, 12002L, 3003L, 0.91D, "文档切片"),
                hit(12205L, 12002L, 3003L, 0.30D, "低分片段"),
                hit(12206L, 12002L, 9999L, 0.88D, "其他课程")));

        RagSearchRequest request = new RagSearchRequest();
        request.setQuery("为什么要切片");
        request.setKnowledgeBaseId(12002L);
        request.setCourseId(3003L);

        List<RetrievedSegment> segments = retriever.searchSegments(request);

        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).segmentId()).isEqualTo(12204L);
        assertThat(segments.get(0).preview()).contains("文档切片");
    }

    private VectorSearchResult hit(Long segmentId, Long kbId, Long courseId, Double score, String title) {
        return VectorSearchResult.builder()
                .vectorId(String.valueOf(segmentId))
                .score(score)
                .payload(QdrantPointPayload.builder()
                        .kbId(kbId)
                        .documentId(12102L)
                        .segmentId(segmentId)
                        .courseId(courseId)
                        .sourceType("PDF")
                        .title(title)
                        .content(title + "可以提升召回精度。")
                        .build())
                .build();
    }
}
