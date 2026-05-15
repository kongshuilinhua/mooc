package com.elysia.mooc.knowledge.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.constants.EventTopicConstants;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.EventPublisher;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeSegmentQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeDocumentSourceType;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.po.KnowledgeBasePO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeDocumentPO;
import com.elysia.mooc.knowledge.domain.po.KnowledgeSegmentPO;
import com.elysia.mooc.knowledge.mapper.KnowledgeBaseMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeDocumentMapper;
import com.elysia.mooc.knowledge.mapper.KnowledgeSegmentMapper;
import com.elysia.mooc.knowledge.parse.DocumentParser;
import com.elysia.mooc.knowledge.parse.DocumentParserFactory;
import com.elysia.mooc.knowledge.parse.TextSegment;
import com.elysia.mooc.knowledge.parse.TextSegmenter;
import com.elysia.mooc.media.config.MediaStorageProperties;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 文档解析服务业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeDocumentParseServiceImplTest {

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
    private MediaStorageProperties mediaStorageProperties;

    @Mock
    private DocumentParserFactory documentParserFactory;

    @Mock
    private TextSegmenter textSegmenter;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private DocumentParser documentParser;

    @InjectMocks
    private KnowledgeDocumentParseServiceImpl service;

    @TempDir
    Path tempDir;

    @Test
    void parseDocumentShouldPersistSegmentsAndPublishEmbeddingEvent() throws Exception {
        Path file = tempDir.resolve("guide.md");
        Files.writeString(file, "# 平台指南\n\n登录和学习说明");
        KnowledgeDocumentPO document = document(KnowledgeDocumentSourceType.MARKDOWN);
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document);
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(enabledBase());
        when(mediaFileMapper.selectById(6001L)).thenReturn(mediaFile(file));
        when(documentParserFactory.getParser(KnowledgeDocumentSourceType.MARKDOWN)).thenReturn(documentParser);
        when(documentParser.parse(file.toAbsolutePath().normalize())).thenReturn("平台指南\n登录和学习说明");
        when(textSegmenter.segment("平台指南\n登录和学习说明")).thenReturn(List.of(
                new TextSegment(1, "平台指南", "平台指南", 4, "{\"segmentIndex\":1}"),
                new TextSegment(2, "登录", "登录和学习说明", 7, "{\"segmentIndex\":2}")));

        var result = service.parseDocument(12101L);

        assertThat(result.getParseStatus()).isEqualTo(KnowledgeProcessStatus.SUCCESS);
        assertThat(result.getSegmentCount()).isEqualTo(2);
        verify(knowledgeSegmentMapper).deleteByDocumentIdPhysically(12101L);
        ArgumentCaptor<KnowledgeSegmentPO> segmentCaptor = ArgumentCaptor.forClass(KnowledgeSegmentPO.class);
        verify(knowledgeSegmentMapper, org.mockito.Mockito.times(2)).insert(segmentCaptor.capture());
        assertThat(segmentCaptor.getAllValues()).extracting(KnowledgeSegmentPO::getSegmentIndex)
                .containsExactly(1, 2);
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getTopic()).isEqualTo(EventTopicConstants.KNOWLEDGE_EMBEDDING_REQUESTED);
        assertThat(eventCaptor.getValue().getBizKey()).isEqualTo("knowledge_document:12101");
    }

    @Test
    void parseDocumentShouldMarkFailedWhenNoValidText() throws Exception {
        Path file = tempDir.resolve("empty.txt");
        Files.writeString(file, " ");
        KnowledgeDocumentPO document = document(KnowledgeDocumentSourceType.TXT);
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document);
        when(knowledgeBaseMapper.selectById(12001L)).thenReturn(enabledBase());
        when(mediaFileMapper.selectById(6001L)).thenReturn(mediaFile(file));
        when(documentParserFactory.getParser(KnowledgeDocumentSourceType.TXT)).thenReturn(documentParser);
        when(documentParser.parse(file.toAbsolutePath().normalize())).thenReturn(" ");
        when(textSegmenter.segment(" ")).thenReturn(List.of());

        assertThatThrownBy(() -> service.parseDocument(12101L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(KnowledgeErrorCode.KNOWLEDGE_STATUS_INVALID.code());
        verify(knowledgeDocumentMapper).update(nullable(KnowledgeDocumentPO.class), any(Wrapper.class));
    }

    @Test
    void listSegmentsShouldReturnPageResult() {
        KnowledgeDocumentPO document = document(KnowledgeDocumentSourceType.MARKDOWN);
        when(userContextService.currentLoginUser()).thenReturn(admin());
        when(knowledgeDocumentMapper.selectById(12101L)).thenReturn(document);
        Page<KnowledgeSegmentPO> page = new Page<>(1, 10);
        KnowledgeSegmentPO segment = new KnowledgeSegmentPO();
        segment.setId(12201L);
        segment.setDocumentId(12101L);
        segment.setSegmentIndex(1);
        segment.setContent("切片内容");
        segment.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        page.setRecords(List.of(segment));
        page.setTotal(1);
        when(knowledgeSegmentMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        KnowledgeSegmentQuery query = new KnowledgeSegmentQuery();
        var result = service.listSegments(12101L, query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
    }

    @Test
    void parseDocumentShouldRejectUserWithoutManagePermission() {
        when(userContextService.currentLoginUser())
                .thenReturn(new LoginUser(4L, "student", List.of("STUDENT"), List.of("ai:chat")));

        assertThatThrownBy(() -> service.parseDocument(12101L))
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
        po.setStatus(EnableStatus.ENABLED);
        return po;
    }

    private KnowledgeDocumentPO document(KnowledgeDocumentSourceType sourceType) {
        KnowledgeDocumentPO po = new KnowledgeDocumentPO();
        po.setId(12101L);
        po.setKbId(12001L);
        po.setMediaFileId(6001L);
        po.setTitle("平台指南");
        po.setSourceType(sourceType);
        po.setSourceUrl("/files/knowledge/doc/guide.md");
        po.setContentHash("hash-guide");
        po.setParseStatus(KnowledgeProcessStatus.PENDING);
        po.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        po.setSegmentCount(0);
        return po;
    }

    private MediaFilePO mediaFile(Path file) {
        MediaFilePO po = new MediaFilePO();
        po.setId(6001L);
        po.setStoragePath(file.toString());
        return po;
    }
}
