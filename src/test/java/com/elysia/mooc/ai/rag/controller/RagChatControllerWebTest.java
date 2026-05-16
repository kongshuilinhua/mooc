package com.elysia.mooc.ai.rag.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.ai.chat.domain.enums.AiMessageStatus;
import com.elysia.mooc.ai.chat.domain.vo.AiSourceVO;
import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.domain.vo.RagChatResult;
import com.elysia.mooc.ai.rag.domain.vo.RagSearchResult;
import com.elysia.mooc.ai.rag.service.RagService;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** day16 RAG 控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class RagChatControllerWebTest {

    @Mock
    private RagService ragService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RagChatController(ragService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chatShouldReturnRagContract() throws Exception {
        RagChatResult result = new RagChatResult();
        result.setConversationId(16001L);
        result.setMessageId(16102L);
        result.setContent("切片可以提升召回精度。");
        result.setAnswer("切片可以提升召回精度。");
        result.setSources(List.of(source()));
        result.setCitations(result.getSources());
        result.setToolCalls(Collections.emptyList());
        result.setStatus(AiMessageStatus.SUCCESS);
        result.setModelName("qwen-plus");
        result.setPromptTokens(100);
        result.setCompletionTokens(30);
        result.setTotalTokens(130);
        result.setFinishReason("stop");
        when(ragService.chat(any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/rag/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"RAG 中为什么要切片？\",\"knowledgeBaseId\":12002,\"topK\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(16001))
                .andExpect(jsonPath("$.data.messageId").value(16102))
                .andExpect(jsonPath("$.data.content").value("切片可以提升召回精度。"))
                .andExpect(jsonPath("$.data.answer").value("切片可以提升召回精度。"))
                .andExpect(jsonPath("$.data.sources[0].kbId").value(12002))
                .andExpect(jsonPath("$.data.sources[0].documentId").value(12102))
                .andExpect(jsonPath("$.data.sources[0].segmentId").value(12204))
                .andExpect(jsonPath("$.data.citations[0].preview").value("上传文档后先解析成纯文本"));

        ArgumentCaptor<RagChatRequest> captor = ArgumentCaptor.forClass(RagChatRequest.class);
        verify(ragService).chat(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getMessage()).contains("RAG");
    }

    @Test
    void searchShouldReturnSources() throws Exception {
        RagSearchResult result = new RagSearchResult();
        result.setQuery("文档切片");
        result.setSources(List.of(source()));
        when(ragService.search(any())).thenReturn(result);

        mockMvc.perform(post("/api/ai/rag/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"文档切片\",\"knowledgeBaseId\":12002,\"topK\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("文档切片"))
                .andExpect(jsonPath("$.data.sources[0].title").value("文档切片"));

        ArgumentCaptor<RagSearchRequest> captor = ArgumentCaptor.forClass(RagSearchRequest.class);
        verify(ragService).search(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getKnowledgeBaseId()).isEqualTo(12002L);
    }

    @Test
    void searchShouldRejectBlankQuery() throws Exception {
        mockMvc.perform(post("/api/ai/rag/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("检索文本不能为空"));
    }

    private AiSourceVO source() {
        AiSourceVO source = new AiSourceVO();
        source.setSourceType("PDF");
        source.setSourceId(12204L);
        source.setKbId(12002L);
        source.setDocumentId(12102L);
        source.setSegmentId(12204L);
        source.setCourseId(3003L);
        source.setTitle("文档切片");
        source.setScore(0.91D);
        source.setPreview("上传文档后先解析成纯文本");
        return source;
    }
}
