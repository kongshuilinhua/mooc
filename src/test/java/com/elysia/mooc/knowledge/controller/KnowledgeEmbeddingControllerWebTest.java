package com.elysia.mooc.knowledge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.knowledge.domain.dto.VectorSearchRequest;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchResponseVO;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchSourceVO;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.service.EmbeddingService;
import com.elysia.mooc.knowledge.service.VectorSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** day14 向量管理控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeEmbeddingControllerWebTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorSearchService vectorSearchService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new KnowledgeEmbeddingController(embeddingService, vectorSearchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rebuildSegmentShouldReturnTrue() throws Exception {
        when(embeddingService.rebuildSegment(12201L)).thenReturn(true);

        mockMvc.perform(post("/api/admin/ai/segments/12201/embedding/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void rebuildDocumentShouldReturnTrue() throws Exception {
        when(embeddingService.rebuildDocument(12101L)).thenReturn(true);

        mockMvc.perform(post("/api/admin/ai/documents/12101/embedding/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void vectorSearchShouldReturnSourcesAndBindRequest() throws Exception {
        VectorSearchResponseVO response = new VectorSearchResponseVO();
        response.setQuery("课程怎么学习");
        VectorSearchSourceVO source = new VectorSearchSourceVO();
        source.setVectorId("12201");
        source.setKbId(12001L);
        source.setDocumentId(12101L);
        source.setSegmentId(12201L);
        source.setTitle("学习进度");
        source.setScore(0.87D);
        source.setContentPreview("学习心跳会上报当前播放位置");
        source.setPayload(QdrantPointPayload.builder()
                .kbId(12001L)
                .documentId(12101L)
                .segmentId(12201L)
                .title("学习进度")
                .content("学习心跳会上报当前播放位置")
                .build());
        response.setSources(List.of(source));
        when(vectorSearchService.search(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/ai/vector-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body("课程怎么学习", 12001L, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("课程怎么学习"))
                .andExpect(jsonPath("$.data.sources[0].segmentId").value(12201))
                .andExpect(jsonPath("$.data.sources[0].score").value(0.87))
                .andExpect(jsonPath("$.data.sources[0].payload.kbId").value(12001));

        ArgumentCaptor<VectorSearchRequest> captor = ArgumentCaptor.forClass(VectorSearchRequest.class);
        verify(vectorSearchService).search(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getKnowledgeBaseId()).isEqualTo(12001L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getTopK()).isEqualTo(3);
    }

    @Test
    void vectorSearchShouldRejectBlankQuery() throws Exception {
        mockMvc.perform(post("/api/admin/ai/vector-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\" \",\"topK\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("检索文本不能为空"));
    }

    private static Map<String, Object> body(String query, Long knowledgeBaseId, Integer topK) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        body.put("knowledgeBaseId", knowledgeBaseId);
        body.put("topK", topK);
        return body;
    }
}
