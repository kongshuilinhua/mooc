package com.elysia.mooc.knowledge.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.audit.AuditLog;
import com.elysia.mooc.common.idempotent.Idempotent;
import com.elysia.mooc.knowledge.domain.dto.CreateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeBaseQuery;
import com.elysia.mooc.knowledge.domain.dto.KnowledgeDocumentQuery;
import com.elysia.mooc.knowledge.domain.dto.UpdateKnowledgeBaseRequest;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeBaseVO;
import com.elysia.mooc.knowledge.domain.vo.KnowledgeDocumentVO;
import com.elysia.mooc.knowledge.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 知识库基础与文档管理接口。 */
@Tag(name = "知识库基础与文档管理")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 分页查询知识库。
     *
     * @param query 查询条件
     * @return 知识库分页
     */
    @Operation(summary = "分页查询知识库")
    @GetMapping("/knowledge-bases")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<PageResult<KnowledgeBaseVO>> listKnowledgeBases(@Valid KnowledgeBaseQuery query) {
        return ApiResult.ok(knowledgeBaseService.listKnowledgeBases(query));
    }

    /**
     * 创建知识库。
     *
     * @param request 创建请求
     * @return 新建知识库
     */
    @Operation(summary = "创建知识库")
    @PostMapping("/knowledge-bases")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<KnowledgeBaseVO> createKnowledgeBase(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return ApiResult.ok(knowledgeBaseService.createKnowledgeBase(request));
    }

    /**
     * 修改知识库。
     *
     * @param id 知识库 ID
     * @param request 修改请求
     * @return 修改后的知识库
     */
    @Operation(summary = "修改知识库")
    @PutMapping("/knowledge-bases/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<KnowledgeBaseVO> updateKnowledgeBase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKnowledgeBaseRequest request) {
        return ApiResult.ok(knowledgeBaseService.updateKnowledgeBase(id, request));
    }

    /**
     * 上传知识库文档。
     *
     * @param id 知识库 ID
     * @param file 文档文件
     * @param title 文档标题
     * @param sourceType 来源类型
     * @return 新建文档
     */
    @Operation(summary = "上传知识库文档")
    @PostMapping("/knowledge-bases/{id}/documents")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<KnowledgeDocumentVO> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("sourceType") String sourceType) {
        return ApiResult.ok(knowledgeBaseService.uploadDocument(id, file, title, sourceType));
    }

    /**
     * 分页查询知识库文档。
     *
     * @param query 查询条件
     * @return 文档分页
     */
    @Operation(summary = "分页查询知识库文档")
    @GetMapping("/documents")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    public ApiResult<PageResult<KnowledgeDocumentVO>> listDocuments(@Valid KnowledgeDocumentQuery query) {
        return ApiResult.ok(knowledgeBaseService.listDocuments(query));
    }

    /**
     * 重建文档索引。
     *
     * @param id 文档 ID
     * @return 是否提交成功
     */
    @Operation(summary = "重建文档索引")
    @PostMapping("/documents/{id}/rebuild")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ai:kb:manage')")
    @AuditLog(action = "KNOWLEDGE_DOCUMENT_REBUILD", targetType = "KNOWLEDGE_DOCUMENT", targetId = "#id")
    @Idempotent(bizType = "KNOWLEDGE_DOCUMENT_REBUILD", bizId = "#id")
    public ApiResult<Boolean> rebuildDocument(@PathVariable Long id) {
        return ApiResult.ok(knowledgeBaseService.rebuildDocument(id));
    }
}
