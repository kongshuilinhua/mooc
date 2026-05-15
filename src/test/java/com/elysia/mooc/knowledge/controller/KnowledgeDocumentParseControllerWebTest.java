package com.elysia.mooc.knowledge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeSegmentQuery;
import com.elysia.mooc.knowledge.domain.enums.KnowledgeProcessStatus;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentParseStatusVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeSegmentVO;
import com.elysia.mooc.knowledge.service.KnowledgeDocumentParseService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 文档解析控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class KnowledgeDocumentParseControllerWebTest {

    @Mock
    private KnowledgeDocumentParseService knowledgeDocumentParseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new KnowledgeDocumentParseController(knowledgeDocumentParseService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void parseDocumentShouldReturnDocumentStatus() throws Exception {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(12101L);
        vo.setKbId(12001L);
        vo.setParseStatus(KnowledgeProcessStatus.SUCCESS);
        vo.setSegmentCount(2);
        when(knowledgeDocumentParseService.parseDocument(12101L)).thenReturn(vo);

        mockMvc.perform(post("/api/admin/ai/documents/12101/parse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(12101))
                .andExpect(jsonPath("$.data.parseStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.data.segmentCount").value(2));
    }

    @Test
    void listSegmentsShouldReturnPageResultAndBindStatus() throws Exception {
        KnowledgeSegmentVO vo = new KnowledgeSegmentVO();
        vo.setId(12201L);
        vo.setDocumentId(12101L);
        vo.setSegmentIndex(1);
        vo.setContent("文档切片内容");
        vo.setEmbeddingStatus(KnowledgeProcessStatus.PENDING);
        when(knowledgeDocumentParseService.listSegments(eq(12101L), any()))
                .thenReturn(PageResult.of(1L, 10, List.of(vo)));

        mockMvc.perform(get("/api/admin/ai/documents/12101/segments")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].segmentIndex").value(1))
                .andExpect(jsonPath("$.data.list[0].embeddingStatus").value("PENDING"));

        ArgumentCaptor<KnowledgeSegmentQuery> captor = ArgumentCaptor.forClass(KnowledgeSegmentQuery.class);
        verify(knowledgeDocumentParseService).listSegments(eq(12101L), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus())
                .isEqualTo(KnowledgeProcessStatus.PENDING);
    }

    @Test
    void getParseStatusShouldReturnStatusVO() throws Exception {
        KnowledgeDocumentParseStatusVO vo = new KnowledgeDocumentParseStatusVO();
        vo.setDocumentId(12101L);
        vo.setKbId(12001L);
        vo.setParseStatus(KnowledgeProcessStatus.FAILED);
        vo.setParseError("文档未提取到有效内容");
        vo.setSegmentCount(0);
        when(knowledgeDocumentParseService.getParseStatus(12101L)).thenReturn(vo);

        mockMvc.perform(get("/api/admin/ai/documents/12101/parse-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentId").value(12101))
                .andExpect(jsonPath("$.data.parseStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.parseError").value("文档未提取到有效内容"));
    }
}
