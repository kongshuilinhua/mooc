package com.elysia.mooc.knowledge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.knowledge.constants.KnowledgeErrorCode;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeBaseQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeScopeType;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeBaseVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.service.KnowledgeBaseService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 知识库控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeBaseControllerWebTest {

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new KnowledgeBaseController(knowledgeBaseService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listKnowledgeBasesShouldReturnPageResultAndBindStatus() throws Exception {
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        vo.setId(12001L);
        vo.setName("平台帮助知识库");
        vo.setCode("GLOBAL_MOOC_HELP");
        vo.setScopeType(KnowledgeScopeType.GLOBAL);
        vo.setStatus(EnableStatus.ENABLED);
        vo.setDocumentCount(1L);
        vo.setSegmentCount(3L);
        vo.setCreateTime(LocalDateTime.of(2026, 5, 15, 10, 0));
        when(knowledgeBaseService.listKnowledgeBases(any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/ai/knowledge-bases")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "ENABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(12001))
                .andExpect(jsonPath("$.data.list[0].status").value(1));

        ArgumentCaptor<KnowledgeBaseQuery> captor = ArgumentCaptor.forClass(KnowledgeBaseQuery.class);
        verify(knowledgeBaseService).listKnowledgeBases(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus()).isEqualTo(EnableStatus.ENABLED);
    }

    @Test
    void listKnowledgeBasesShouldReturn403WhenServiceRejectsStudent() throws Exception {
        when(knowledgeBaseService.listKnowledgeBases(any()))
                .thenThrow(new BizException(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN));

        mockMvc.perform(get("/api/ai/knowledge-bases"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(KnowledgeErrorCode.KNOWLEDGE_FORBIDDEN.message()));
    }

    @Test
    void uploadDocumentShouldAcceptMultipartContract() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "guide.pdf", "application/pdf", "pdf".getBytes());
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(12100L);
        vo.setKbId(12001L);
        vo.setKnowledgeBaseId(12001L);
        vo.setTitle("平台指南");
        vo.setParseStatus(KnowledgeProcessStatus.PENDING);
        vo.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        vo.setContentHash("hash");
        vo.setSegmentCount(0);
        when(knowledgeBaseService.uploadDocument(eq(12001L), any(), eq("平台指南"), eq("upload")))
                .thenReturn(vo);

        mockMvc.perform(multipart("/api/ai/knowledge-bases/12001/documents")
                        .file(file)
                        .param("title", "平台指南")
                        .param("sourceType", "upload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(12100))
                .andExpect(jsonPath("$.data.knowledgeBaseId").value(12001))
                .andExpect(jsonPath("$.data.parseStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.embeddingStatus").value("PENDING"));
    }

    @Test
    void rebuildDocumentShouldReturnBoolean() throws Exception {
        when(knowledgeBaseService.rebuildDocument(12100L)).thenReturn(true);

        mockMvc.perform(post("/api/ai/documents/12100/rebuild"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }
}
