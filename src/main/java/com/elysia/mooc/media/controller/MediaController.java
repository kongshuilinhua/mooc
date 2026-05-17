package com.elysia.mooc.media.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.audit.AuditLog;
import com.elysia.mooc.common.idempotent.Idempotent;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.dto.MediaFileQuery;
import com.elysia.mooc.media.domain.dto.MergeChunksRequest;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.vo.MediaFileItem;
import com.elysia.mooc.media.domain.vo.MergeChunksResult;
import com.elysia.mooc.media.domain.vo.UploadChunkResult;
import com.elysia.mooc.media.service.MediaFileService;
import com.elysia.mooc.common.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 媒资上传、分片和文件管理接口。 */
@Tag(name = "媒资管理")
@Validated
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaFileService mediaFileService;

    /**
     * 普通上传媒资文件。
     *
     * @param file 上传文件
     * @param bizType 业务类型
     * @return 媒资文件信息
     */
    @Operation(summary = "普通上传媒资文件")
    @PostMapping("/files")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<MediaFileItem> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") MediaBizType bizType) {
        return ApiResult.ok(mediaFileService.uploadFile(file, bizType));
    }

    /**
     * 上传媒资分片。
     *
     * @param file 当前分片文件
     * @param fileHash 文件摘要，正式字段
     * @param fileMd5 文件 MD5，兼容前端旧字段
     * @param chunkIndex 分片序号，从 0 开始
     * @param totalChunks 分片总数
     * @param fileName 原始文件名
     * @param bizType 业务类型
     * @return 分片上传结果
     */
    @Operation(summary = "上传媒资分片")
    @PostMapping("/chunks")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<UploadChunkResult> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileHash", required = false) String fileHash,
            @RequestParam(value = "fileMd5", required = false) String fileMd5,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("totalChunks") Integer totalChunks,
            @RequestParam("fileName") String fileName,
            @RequestParam("bizType") MediaBizType bizType) {
        return ApiResult.ok(mediaFileService.uploadChunk(
                file, resolveFileHash(fileHash, fileMd5), chunkIndex, totalChunks, fileName, bizType));
    }

    /**
     * 合并媒资分片。
     *
     * @param request 合并请求
     * @return 合并后的媒资文件信息
     */
    @Operation(summary = "合并媒资分片")
    @PostMapping("/chunks/merge")
    @PreAuthorize("isAuthenticated()")
    @ParamChecker
    @AuditLog(action = "MEDIA_CHUNK_MERGE", targetType = "MEDIA_FILE", targetId = "#request.resolvedFileHash()")
    @Idempotent(bizType = "MEDIA_CHUNK_MERGE", bizId = "#request.resolvedFileHash()")
    public ApiResult<MergeChunksResult> mergeChunks(@Valid @RequestBody MergeChunksRequest request) {
        return ApiResult.ok(mediaFileService.mergeChunks(request));
    }

    /**
     * 分页查询媒资文件。
     *
     * @param query 查询条件
     * @return 媒资分页
     */
    @Operation(summary = "分页查询媒资文件")
    @GetMapping("/files")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<PageResult<MediaFileItem>> listFiles(@Valid MediaFileQuery query) {
        return ApiResult.ok(mediaFileService.listFiles(query));
    }

    /**
     * 删除媒资文件。
     *
     * @param fileId 媒资文件 ID
     * @return 是否删除成功
     */
    @Operation(summary = "删除媒资文件")
    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<Boolean> deleteFile(@PathVariable Long fileId) {
        return ApiResult.ok(mediaFileService.deleteFile(fileId));
    }

    /**
     * 解析文件摘要字段。
     *
     * @param fileHash 新合同字段，优先使用
     * @param fileMd5 旧前端字段，仅作为兼容兜底
     * @return 归一化后的文件摘要
     */
    private String resolveFileHash(String fileHash, String fileMd5) {
        // fileHash 是 day06 正式口径，保留 fileMd5 是为了让旧前端分片上传在迁移期不中断。
        if (StringUtils.hasText(fileHash)) {
            return fileHash.trim();
        }
        if (StringUtils.hasText(fileMd5)) {
            return fileMd5.trim();
        }
        throw new BizException(MediaErrorCode.MEDIA_PARAM_INVALID, "文件摘要不能为空");
    }
}
