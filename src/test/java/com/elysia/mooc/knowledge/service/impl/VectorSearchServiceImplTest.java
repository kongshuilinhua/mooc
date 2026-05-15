package com.elysia.mooc.knowledge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.knowledge.domain.dto.VectorSearchRequest;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchResponseVO;
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

/** 向量检索服务测试。 */
@ExtendWith(MockitoExtension.class)
class VectorSearchServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private EmbeddingClient embeddingClient;

    @Mock
    private QdrantClient qdrantClient;

    private VectorSearchServiceImpl vectorSearchService;

    @BeforeEach
    void setUp() {
        vectorSearchService = new VectorSearchServiceImpl(
                userContextService,
                new EmbeddingProperties(),
                new QdrantProperties(),
                embeddingClient,
                qdrantClient);
    }

    @Test
    void searchShouldGenerateEmbeddingAndReturnPreview() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(1L, "admin", List.of("ADMIN"), List.of("ai:kb:manage")));
        when(embeddingClient.embed(any(EmbeddingRequest.class)))
                .thenReturn(new EmbeddingResult(Collections.nCopies(1024, 0.2F), "text-embedding-v4", 1024));
        when(qdrantClient.search(any(), org.mockito.ArgumentMatchers.eq(12001L), org.mockito.ArgumentMatchers.eq(2)))
                .thenReturn(List.of(VectorSearchResult.builder()
                        .vectorId("12201")
                        .score(0.91D)
                        .payload(QdrantPointPayload.builder()
                                .kbId(12001L)
                                .documentId(12101L)
                                .segmentId(12201L)
                                .title("学习进度")
                                .content("学习心跳会上报当前播放位置、学习时长和完成状态，用于计算课程进度。")
                                .build())
                        .build()));
        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery("课程怎么学习");
        request.setKnowledgeBaseId(12001L);
        request.setTopK(2);

        VectorSearchResponseVO result = vectorSearchService.search(request);

        assertThat(result.getQuery()).isEqualTo("课程怎么学习");
        assertThat(result.getSources()).hasSize(1);
        assertThat(result.getSources().get(0).getSegmentId()).isEqualTo(12201L);
        assertThat(result.getSources().get(0).getContentPreview()).contains("学习心跳");
    }
}
