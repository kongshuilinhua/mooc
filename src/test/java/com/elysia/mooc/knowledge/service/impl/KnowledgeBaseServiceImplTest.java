package com.elysia.mooc.knowledge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.CreateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import com.elysia.mooc.media.service.MediaStorageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

/** 知识库服务业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Mock
    private KnowledgeSegmentMapper knowledgeSegmentMapper;

    @Mock
    private MediaFileMapper mediaFileMapper;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private KnowledgeBaseServiceImpl knowledgeBaseService;

    @Test
    void createKnowledgeBaseShouldGenerateCodeAndCourseScope() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeBaseMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest();
        request.setName("RAG 课程知识库");
        request.setCourseId(3003L);

        knowledgeBaseService.createKnowledgeBase(request);

        ArgumentCaptor<KnowledgeBasePO> captor = ArgumentCaptor.forClass(KnowledgeBasePO.class);
        verify(knowledgeBaseMapper).insert(captor.capture());
        assertThat(captor.getValue().getCode()).startsWith("KB_COURSE_3003_");
        assertThat(captor.getValue().getScopeType()).isEqualTo(KnowledgeScopeType.COURSE);
    }

    @Test
    void createKnowledgeBaseShouldRejectDuplicatedNameOrCode() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeBaseMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest();
        request.setName("平台帮助知识库");

        assertThatThrownBy(() -> knowledgeBaseService.createKnowledgeBase(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(KnowledgeErrorCode.KNOWLEDGE_BASE_DUPLICATED.code());
    }

    @Test
    void uploadDocumentShouldStorePendingDocumentAndDeduplicateByHash() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(enabledBase());
        when(mediaStorageService.storeFile(any(), any()))
                .thenReturn(new MediaStorageService.StoredFile(
                        "guide.pdf",
                        "D:/mooc-storage/knowledge/doc/guide.pdf",
                        "/files/knowledge/doc/guide.pdf",
                        "application/pdf",
                        3L,
                        "hash-guide"));
        when(knowledgeDocumentMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        KnowledgeDocumentVO result = knowledgeBaseService.uploadDocument(
                12001L,
                new MockMultipartFile("file", "guide.pdf", "application/pdf", "pdf".getBytes()),
                "平台指南",
                "upload");

        assertThat(result.getParseStatus()).isEqualTo(KnowledgeProcessStatus.PENDING);
        assertThat(result.getEmbeddingStatus()).isEqualTo(KnowledgeProcessStatus.PENDING);
        ArgumentCaptor<KnowledgeDocumentPO> captor = ArgumentCaptor.forClass(KnowledgeDocumentPO.class);
        verify(knowledgeDocumentMapper).insert(captor.capture());
        assertThat(captor.getValue().getSourceType()).isEqualTo(KnowledgeDocumentSourceType.PDF);
        assertThat(captor.getValue().getContentHash()).isEqualTo("hash-guide");
    }

    @Test
    void uploadDocumentShouldRejectDuplicateHashInSameKnowledgeBase() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(enabledBase());
        when(mediaStorageService.storeFile(any(), any()))
                .thenReturn(new MediaStorageService.StoredFile(
                        "guide.pdf", "path", "/files/guide.pdf", "application/pdf", 3L, "hash-guide"));
        when(knowledgeDocumentMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> knowledgeBaseService.uploadDocument(
                12001L,
                new MockMultipartFile("file", "guide.pdf", "application/pdf", "pdf".getBytes()),
                "平台指南",
                "upload"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(KnowledgeErrorCode.KNOWLEDGE_DOCUMENT_DUPLICATED.code());
    }

    @Test
    void rebuildDocumentShouldResetStatusAndPublishEmbeddingEvent() {
        when(userContextService.currentLoginUser()).thenReturn(admin());
        KnowledgeDocumentPO document = document();
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document);
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(enabledBase());

        Boolean result = knowledgeBaseService.rebuildDocument(12101L);

        assertThat(result).isTrue();
        assertThat(document.getParseStatus()).isEqualTo(KnowledgeProcessStatus.PENDING);
        assertThat(document.getEmbeddingStatus()).isEqualTo(KnowledgeProcessStatus.PENDING);
        verify(knowledgeDocumentMapper).updateById(document);
        verify(knowledgeSegmentMapper).update(nullable(KnowledgeSegmentPO.class), any(Wrapper.class));
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getTopic()).isEqualTo(EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED);
        assertThat(captor.getValue().getEventType()).isEqualTo(EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED);
        assertThat(captor.getValue().getBizKey()).isEqualTo("knowledge_document:12101");
    }

    @Test
    void listShouldRejectUserWithoutKnowledgePermission() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(4L, "student", List.of("STUDENT"), List.of("ai:chat")));

        assertThatThrownBy(() -> knowledgeBaseService.listKnowledgeBases(null))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN.code());
    }

    private LoginUser admin() {
        return new LoginUser(1L, "admin", List.of("ADMIN"), List.of("ai:kb:manage"));
    }

    private KnowledgeBasePO enabledBase() {
        KnowledgeBasePO po = new KnowledgeBasePO();
        po.setId(12001L);
        po.setName("平台帮助知识库");
        po.setCode("GLOBAL_MOOC_HELP");
        po.setScopeType(KnowledgeScopeType.GLOBAL);
        po.setStatus(com.elysia.mooc.common.enums.EnableStatus.ENABLED);
        return po;
    }

    private KnowledgeDocumentPO document() {
        KnowledgeDocumentPO po = new KnowledgeDocumentPO();
        po.setId(12101L);
        po.setKbId(12001L);
        po.setTitle("平台指南");
        po.setSourceUrl("/files/guide.pdf");
        po.setContentHash("hash-guide");
        po.setParseStatus(KnowledgeProcessStatus.SUCCESS);
        po.setEmbeddingStatus(KnowledgeProcessStatus.SUCCESS);
        po.setSegmentCount(3);
        return po;
    }
}
