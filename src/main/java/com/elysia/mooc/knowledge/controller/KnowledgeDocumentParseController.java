package com.elysia.mooc.knowledge.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeSegmentQuery;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentParseStatusVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeSegmentVO;
import com.elysia.mooc.knowledge.service.KnowledgeDocumentParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 文档解析与切片管理接口。 */
@Tag(name = "文档解析与切片")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai/documents")
public class KnowledgeDocumentParseController {

    private final KnowledgeDocumentParseService knowledgeDocumentParseService;

    /**
     * 手动触发文档解析。
     *
     * @param documentId 文档 ID
     * @return 解析后的文档信息
     */
    @Operation(summary = "手动触发文档解析")
    @PostMapping("/{documentId}/parse")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<KnowledgeDocumentVO> parseDocument(@PathVariable Long documentId) {
        return ApiResult.ok(knowledgeDocumentParseService.parseDocument(documentId));
    }

    /**
     * 分页查询文档切片。
     *
     * @param documentId 文档 ID
     * @param query 查询条件
     * @return 切片分页
     */
    @Operation(summary = "分页查询文档切片")
    @GetMapping("/{documentId}/segments")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<PageResult<KnowledgeSegmentVO>> listSegments(
            @PathVariable Long documentId,
            @Valid KnowledgeSegmentQuery query) {
        return ApiResult.ok(knowledgeDocumentParseService.listSegments(documentId, query));
    }

    /**
     * 查询文档解析状态。
     *
     * @param documentId 文档 ID
     * @return 解析状态
     */
    @Operation(summary = "查询文档解析状态")
    @GetMapping("/{documentId}/parse-status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<KnowledgeDocumentParseStatusVO> getParseStatus(@PathVariable Long documentId) {
        return ApiResult.ok(knowledgeDocumentParseService.getParseStatus(documentId));
    }
}
