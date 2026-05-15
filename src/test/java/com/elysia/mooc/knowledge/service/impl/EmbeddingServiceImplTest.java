package com.elysia.mooc.knowledge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.payload.KnowledgeEmbeddingRequestedPayload;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.embedding.EmbeddingClient;
import com.elysia.mooc.knowledge.embedding.EmbeddingProperties;
import com.elysia.mooc.knowledge.embedding.EmbeddingRequest;
import com.elysia.mooc.knowledge.embedding.EmbeddingResult;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.knowledge.qdrant.QdrantClient;
import com.elysia.mooc.knowledge.qdrant.QdrantPointPayload;
import com.elysia.mooc.knowledge.qdrant.QdrantProperties;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** day14 向量化服务业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Mock
    private KnowledgeSegmentMapper knowledgeSegmentMapper;

    @Mock
    private EmbeddingClient embeddingClient;

    @Mock
    private QdrantClient qdrantClient;

    @Mock
    private EventPublisher eventPublisher;

    private EmbeddingServiceImpl embeddingService;

    @BeforeEach
    void setUp() {
        EmbeddingProperties embeddingProperties = new EmbeddingProperties();
        QdrantProperties qdrantProperties = new QdrantProperties();
        embeddingService = new EmbeddingServiceImpl(
                userContextService,
                knowledgeBaseMapper,
                knowledgeDocumentMapper,
                knowledgeSegmentMapper,
                embeddingProperties,
                qdrantProperties,
                embeddingClient,
                qdrantClient,
                eventPublisher);
    }

    @Test
    void rebuildSegmentShouldWriteVectorAndPayload() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeSegmentMapper.selectById(12201L)).thenReturn(segment(12201L, null, KnowledgeProcessStatus.PENDING));
        when(knowledgeDocumentMapper.selectById(12101L))
                .thenReturn(
                        document(KnowledgeProcessStatus.SUCCESS),
                        document(KnowledgeProcessStatus.SUCCESS),
                        embeddedDocument(KnowledgeProcessStatus.SUCCESS));
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(base());
        when(embeddingClient.embed(any(EmbeddingRequest.class))).thenReturn(vector(1024));
        when(knowledgeSegmentMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(segment(12201L, "12201", KnowledgeProcessStatus.SUCCESS)));

        Boolean result = embeddingService.rebuildSegment(12201L);

        assertThat(result).isTrue();
        ArgumentCaptor<QdrantPointPayload> payloadCaptor = ArgumentCaptor.forClass(QdrantPointPayload.class);
        verify(qdrantClient).upsertPoint(eq("12201"), any(), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getKbId()).isEqualTo(12001L);
        assertThat(payloadCaptor.getValue().getDocumentId()).isEqualTo(12101L);
        assertThat(payloadCaptor.getValue().getSegmentId()).isEqualTo(12201L);
        assertThat(payloadCaptor.getValue().getCourseId()).isEqualTo(3003L);
        verify(eventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    void rebuildSegmentShouldDeleteOldVectorWhenForce() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeSegmentMapper.selectById(12201L)).thenReturn(segment(12201L, "old-vector", KnowledgeProcessStatus.SUCCESS));
        when(knowledgeDocumentMapper.selectById(12101L))
                .thenReturn(
                        document(KnowledgeProcessStatus.SUCCESS),
                        document(KnowledgeProcessStatus.SUCCESS),
                        embeddedDocument(KnowledgeProcessStatus.SUCCESS));
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(base());
        when(embeddingClient.embed(any(EmbeddingRequest.class))).thenReturn(vector(1024));
        when(knowledgeSegmentMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(segment(12201L, "12201", KnowledgeProcessStatus.SUCCESS)));

        embeddingService.rebuildSegment(12201L);

        verify(qdrantClient).deletePoint("old-vector");
    }

    @Test
    void eventShouldSkipSuccessSegmentsWithoutForce() {
        KnowledgeEmbeddingRequestedPayload payload = new KnowledgeEmbeddingRequestedPayload(
                12001L, 12101L, 1L, "文档", "/files/doc.pdf", "hash", LocalDateTime.now());
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document(KnowledgeProcessStatus.SUCCESS));
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(base());
        when(knowledgeSegmentMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(segment(12201L, "12201", KnowledgeProcessStatus.SUCCESS)));

        embeddingService.handleEmbeddingRequested(payload);

        verify(embeddingClient, never()).embed(any());
        verify(qdrantClient, never()).upsertPoint(any(), any(), any());
    }

    @Test
    void rebuildDocumentShouldRejectUnparsedDocument() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document(KnowledgeProcessStatus.PENDING));
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(base());

        assertThatThrownBy(() -> embeddingService.rebuildDocument(12101L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID.code());
    }

    @Test
    void rebuildSegmentShouldMarkFailedWhenDimensionMismatch() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeSegmentMapper.selectById(12201L)).thenReturn(segment(12201L, null, KnowledgeProcessStatus.PENDING));
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document(KnowledgeProcessStatus.SUCCESS));
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(base());
        when(embeddingClient.embed(any(EmbeddingRequest.class))).thenReturn(vector(3));
        when(knowledgeSegmentMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(segment(12201L, null, KnowledgeProcessStatus.FAILED)));

        assertThatThrownBy(() -> embeddingService.rebuildSegment(12201L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("向量维度");
        verify(qdrantClient, never()).upsertPoint(any(), any(), any());
        verify(knowledgeSegmentMapper, org.mockito.Mockito.atLeastOnce()).update(any(), any(Wrapper.class));
    }

    private LoginUser admin() {
        return new LoginUser(1L, "admin", List.of("ADMIN"), List.of("ai:kb:manage"));
    }

    private KnowledgeBasePO base() {
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(12001L);
        po.setCourseId(3003L);
        po.setStatus(EnableStatus.ENABLED);
        return po;
    }

    private KnowledgeDocumentPO document(KnowledgeProcessStatus parseStatus) {
        KnowledgeDocumentPO po = new KnowledgeDocumentPO();
        po.setId(12101L);
        po.setKbId(12001L);
        po.setTitle("RAG 讲义");
        po.setSourceType(KnowledgeDocumentSourceType.PDF);
        po.setSourceUrl("/files/rag.pdf");
        po.setContentHash("hash");
        po.setParseStatus(parseStatus);
        po.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        return po;
    }

    private KnowledgeDocumentPO embeddedDocument(KnowledgeProcessStatus parseStatus) {
        KnowledgeDocumentPO po = document(parseStatus);
        po.setEmbeddingStatus(KnowledgeProcessStatus.SUCCESS);
        return po;
    }

    private KnowledgeSegmentPO segment(Long id, String vectorId, KnowledgeProcessStatus status) {
        KnowledgeSegmentPO po = new KnowledgeSegmentPO();
        po.setId(id);
        po.setKbId(12001L);
        po.setDocumentId(12101L);
        po.setSegmentIndex(1);
        po.setTitle("Embedding");
        po.setContent("每个 segment 会调用向量模型生成 embedding，并回写 vector_id。");
        po.setMetadata("{\"chapter\":\"向量化\"}");
        po.setVectorId(vectorId);
        po.setEmbeddingStatus(status);
        return po;
    }

    private EmbeddingResult vector(int size) {
        return new EmbeddingResult(java.util.Collections.nCopies(size, 0.1F), "text-embedding-v4", size);
    }
}
