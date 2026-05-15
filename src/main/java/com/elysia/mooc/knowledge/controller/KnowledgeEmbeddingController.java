package com.elysia.mooc.knowledge.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.knowledge.domain.dto.VectorSearchRequest;
import com.elysia.mooc.knowledge.domain.vo.VectorSearchResponseVO;
import com.elysia.mooc.knowledge.service.EmbeddingService;
import com.elysia.mooc.knowledge.service.VectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Qdrant 与 Embedding 管理接口。 */
@Tag(name = "Qdrant 与 Embedding")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai")
public class KnowledgeEmbeddingController {

    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;

    /**
     * 重建单个切片向量。
     *
     * @param segmentId 切片 ID
     * @return 是否处理成功
     */
    @Operation(summary = "重建单个切片向量")
    @PostMapping("/segments/{segmentId}/embedding/rebuild")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<Boolean> rebuildSegment(@PathVariable Long segmentId) {
        return ApiResult.ok(embeddingService.rebuildSegment(segmentId));
    }

    /**
     * 重建文档全部切片向量。
     *
     * @param documentId 文档 ID
     * @return 是否处理成功
     */
    @Operation(summary = "重建文档全部切片向量")
    @PostMapping("/documents/{documentId}/embedding/rebuild")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<Boolean> rebuildDocument(@PathVariable Long documentId) {
        return ApiResult.ok(embeddingService.rebuildDocument(documentId));
    }

    /**
     * 向量检索调试。
     *
     * @param request 检索请求
     * @return 检索命中来源
     */
    @Operation(summary = "向量检索调试")
    @PostMapping("/vector-search")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<VectorSearchResponseVO> vectorSearch(@Valid @RequestBody VectorSearchRequest request) {
        return ApiResult.ok(vectorSearchService.search(request));
    }
}
