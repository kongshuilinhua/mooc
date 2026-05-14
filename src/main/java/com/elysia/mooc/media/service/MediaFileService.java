package com.elysia.mooc.media.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.media.domain.dto.MediaFileQuery;
import com.elysia.mooc.media.domain.dto.MergeChunksRequest;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.vo.MediaFileItem;
import com.elysia.mooc.media.domain.vo.MergeChunksResult;
import com.elysia.mooc.media.domain.vo.UploadChunkResult;
import org.springframework.web.multipart.MultipartFile;

/** 媒资文件服务。 */
public interface MediaFileService {

    /**
     * 普通文件上传。
     *
     * @param file    上传文件
     * @param bizType 业务类型
     * @return 媒资文件
     */
    MediaFileItem uploadFile(MultipartFile file, MediaBizType bizType);

    /**
     * 上传分片。
     *
     * @param file        当前分片
     * @param fileHash    文件摘要
     * @param chunkIndex  分片序号
     * @param totalChunks 分片总数
     * @param fileName    原文件名
     * @param bizType     业务类型
     * @return 分片上传结果
     */
    UploadChunkResult uploadChunk(
            MultipartFile file,
            String fileHash,
            Integer chunkIndex,
            Integer totalChunks,
            String fileName,
            MediaBizType bizType);

    /**
     * 合并分片。
     *
     * @param request 合并请求
     * @return 合并结果
     */
    MergeChunksResult mergeChunks(MergeChunksRequest request);

    /**
     * 分页查询媒资文件。
     *
     * @param query 查询条件
     * @return 媒资文件分页
     */
    PageResult<MediaFileItem> listFiles(MediaFileQuery query);

    /**
     * 删除媒资文件。
     *
     * @param fileId 文件 ID
     * @return 是否删除成功
     */
    Boolean deleteFile(Long fileId);
}
