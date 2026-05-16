package com.elysia.mooc.ai.rag.controller;

import com.elysia.mooc.ai.rag.domain.dto.RagChatRequest;
import com.elysia.mooc.ai.rag.domain.dto.RagSearchRequest;
import com.elysia.mooc.ai.rag.domain.vo.RagChatResult;
import com.elysia.mooc.ai.rag.domain.vo.RagSearchResult;
import com.elysia.mooc.ai.rag.service.RagService;
import com.elysia.mooc.common.api.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RAG 智能问答接口。 */
@Tag(name = "RAG 智能问答")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/rag")
public class RagChatController {

    private final RagService ragService;

    /**
     * 非流式 RAG 问答。
     *
     * @param request 问答请求
     * @return 回答和引用来源
     */
    @Operation(summary = "RAG 问答")
    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<RagChatResult> chat(@Valid @RequestBody RagChatRequest request) {
        return ApiResult.ok(ragService.chat(request));
    }

    /**
     * RAG 检索预览。
     *
     * @param request 检索请求
     * @return 命中来源
     */
    @Operation(summary = "RAG 检索预览")
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<RagSearchResult> search(@Valid @RequestBody RagSearchRequest request) {
        return ApiResult.ok(ragService.search(request));
    }
}
