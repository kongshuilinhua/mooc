package com.elysia.mooc.media.service;

import com.elysia.mooc.media.domain.enums.MediaBizType;
import org.springframework.web.multipart.MultipartFile;

/** 媒资本地存储服务。 */
public interface MediaStorageService {

    /**
     * 保存普通上传文件。
     *
     * @param file    上传文件
     * @param bizType 业务类型
     * @return 已保存文件信息
     */
    StoredFile storeFile(MultipartFile file, MediaBizType bizType);

    /**
     * 保存上传分片。
     *
     * @param file        当前分片
     * @param fileHash    文件摘要
     * @param chunkIndex  分片序号
     * @param totalChunks 分片总数
     * @param fileName    原文件名
     * @return 分片结果
     */
    StoredChunk storeChunk(MultipartFile file, String fileHash, Integer chunkIndex, Integer totalChunks, String fileName);

    /**
     * 合并分片。
     *
     * @param fileHash    文件摘要
     * @param fileName    原文件名
     * @param totalChunks 分片总数
     * @param bizType     业务类型
     * @return 已保存文件信息
     */
    StoredFile mergeChunks(String fileHash, String fileName, Integer totalChunks, MediaBizType bizType);

    /** 已保存文件信息。 */
    record StoredFile(
            String originalName,
            String storagePath,
            String url,
            String contentType,
            Long fileSize,
            String fileHash) {
    }

    /** 已保存分片信息。 */
    record StoredChunk(
            String fileName,
            String contentType,
            Long fileSize,
            Integer chunkIndex,
            Boolean uploaded) {
    }
}
