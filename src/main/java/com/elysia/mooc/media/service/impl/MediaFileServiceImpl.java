package com.elysia.mooc.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.utils.BeanCopyUtils;
import com.elysia.mooc.course.domain.po.CourseSectionPO;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.media.constants.MediaConstants;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.dto.MediaFileQuery;
import com.elysia.mooc.media.domain.dto.MergeChunksRequest;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaParseStatus;
import com.elysia.mooc.media.domain.enums.MediaTranscodeStatus;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import com.elysia.mooc.media.domain.po.MediaDocumentPO;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import com.elysia.mooc.media.domain.po.MediaVideoPO;
import com.elysia.mooc.media.domain.vo.MediaFileItem;
import com.elysia.mooc.media.domain.vo.MergeChunksResult;
import com.elysia.mooc.media.domain.vo.UploadChunkResult;
import com.elysia.mooc.media.mapper.MediaDocumentMapper;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import com.elysia.mooc.media.mapper.MediaVideoMapper;
import com.elysia.mooc.media.service.MediaFileService;
import com.elysia.mooc.media.service.MediaStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 媒资文件服务实现。 */
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final UserContextService userContextService;
    private final MediaStorageService mediaStorageService;
    private final MediaFileMapper mediaFileMapper;
    private final MediaVideoMapper mediaVideoMapper;
    private final MediaDocumentMapper mediaDocumentMapper;
    private final CourseSectionMapper courseSectionMapper;
    private final BusinessEventPublisher businessEventPublisher;

    /**
     * 普通文件上传。
     *
     * @param file    上传文件
     * @param bizType 业务类型
     * @return 媒资文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaFileItem uploadFile(MultipartFile file, MediaBizType bizType) {
        // 1. 媒资入口必须先按业务类型鉴权，避免学生直接上传课程视频或封面。
        LoginUser loginUser = requireUploadPermission(bizType);

        // 2. 存储层负责类型、大小、路径安全和 hash 计算，业务层只处理元数据和权限。
        MediaStorageService.StoredFile storedFile = mediaStorageService.storeFile(file, bizType);

        // 3. 同一用户、同一业务类型、同一 hash 复用媒资记录，避免重复落库和重复业务记录。
        MediaFilePO mediaFile = findReusableFile(loginUser.getUserId(), storedFile.fileHash(), bizType);
        if (mediaFile == null) {
            mediaFile = createMediaFile(loginUser.getUserId(), bizType, storedFile);
            createBizRecordIfNeeded(mediaFile, bizType);
        }
        return toMediaFileItem(mediaFile);
    }

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
    @Override
    public UploadChunkResult uploadChunk(
            MultipartFile file,
            String fileHash,
            Integer chunkIndex,
            Integer totalChunks,
            String fileName,
            MediaBizType bizType) {
        // 分片上传也要提前鉴权，避免无权限用户通过临时目录消耗本地磁盘。
        requireUploadPermission(bizType);
        String safeHash = normalizeHash(fileHash);
        MediaStorageService.StoredChunk chunk = mediaStorageService.storeChunk(
                file, safeHash, chunkIndex, totalChunks, fileName);
        return UploadChunkResult.builder()
                .fileName(chunk.fileName())
                .contentType(chunk.contentType())
                .fileSize(chunk.fileSize())
                .uploaded(chunk.uploaded())
                .chunkIndex(chunk.chunkIndex())
                .build();
    }

    /**
     * 合并分片。
     *
     * @param request 合并请求
     * @return 合并结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MergeChunksResult mergeChunks(MergeChunksRequest request) {
        // 1. 合并是最终生成媒资记录的写操作，必须重新鉴权，不能只信任分片上传阶段。
        LoginUser loginUser = requireUploadPermission(request.getBizType());
        String fileHash = normalizeHash(request.resolvedFileHash());

        // 2. 重复合并同一文件时直接复用记录，保证前端重试不会产生多条相同媒资。
        MediaFilePO mediaFile = findReusableFile(loginUser.getUserId(), fileHash, request.getBizType());
        if (mediaFile == null) {
            MediaStorageService.StoredFile storedFile = mediaStorageService.mergeChunks(
                    fileHash, request.getFileName(), request.getTotalChunks(), request.getBizType());
            mediaFile = createMediaFile(loginUser.getUserId(), request.getBizType(), storedFile);
            createBizRecordIfNeeded(mediaFile, request.getBizType());
        }
        return toMergeResult(mediaFile);
    }

    /**
     * 分页查询媒资文件。
     *
     * @param query 查询条件
     * @return 媒资文件分页
     */
    @Override
    public PageResult<MediaFileItem> listFiles(MediaFileQuery query) {
        LoginUser loginUser = userContextService.currentLoginUser();
        MediaFileQuery safeQuery = query == null ? new MediaFileQuery() : query;
        LambdaQueryWrapper<MediaFilePO> wrapper = Wrappers.<MediaFilePO>lambdaQuery();

        // 普通用户只能看自己的媒资；管理员保留全量排查和后台管理能力。
        if (!isAdmin(loginUser)) {
            wrapper.eq(MediaFilePO::getOwnerId, loginUser.getUserId());
        }
        if (safeQuery.getBizType() != null) {
            wrapper.eq(MediaFilePO::getBizType, safeQuery.getBizType());
        }

        // status 是旧合同别名，新代码优先使用 uploadStatus，避免前端迁移期查询失效。
        MediaUploadStatus uploadStatus = safeQuery.getUploadStatus() == null
                ? safeQuery.getStatus()
                : safeQuery.getUploadStatus();
        if (uploadStatus != null) {
            wrapper.eq(MediaFilePO::getUploadStatus, uploadStatus);
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(MediaFilePO::getOriginalName, safeQuery.getKeyword().trim());
        }
        wrapper.orderByDesc(MediaFilePO::getCreateTime).orderByDesc(MediaFilePO::getId);
        Page<MediaFilePO> page = new Page<>(safeQuery.getPageNo(), safeQuery.getPageSize());
        return PageResult.of(mediaFileMapper.selectPage(page, wrapper), this::toMediaFileItem);
    }

    /**
     * 删除媒资文件。
     *
     * @param fileId 文件 ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFile(Long fileId) {
        LoginUser loginUser = userContextService.currentLoginUser();
        if (fileId == null || fileId <= 0) {
            throw new BizException(MediaErrorCode.MEDIA_FILE_NOT_FOUND);
        }
        MediaFilePO mediaFile = mediaFileMapper.selectById(fileId);
        if (mediaFile == null) {
            throw new BizException(MediaErrorCode.MEDIA_FILE_NOT_FOUND);
        }
        if (!isAdmin(loginUser) && !loginUser.getUserId().equals(mediaFile.getOwnerId())) {
            throw new BizException(MediaErrorCode.MEDIA_FORBIDDEN);
        }

        // 被小节引用的媒资不能删除，否则课程目录会出现可播放小节却找不到视频的断链。
        Long sectionCount = courseSectionMapper.selectCount(Wrappers.<CourseSectionPO>lambdaQuery()
                .eq(CourseSectionPO::getMediaId, fileId));
        if (sectionCount > 0) {
            throw new BizException(MediaErrorCode.MEDIA_IN_USE);
        }
        mediaFileMapper.deleteById(fileId);
        return true;
    }

    /**
     * 创建媒资主表记录。
     *
     * @param ownerId 媒资拥有者用户 ID
     * @param bizType 业务类型
     * @param storedFile 已保存文件信息
     * @return 已落库的媒资记录
     */
    private MediaFilePO createMediaFile(Long ownerId, MediaBizType bizType, MediaStorageService.StoredFile storedFile) {
        // 默认值和状态流转字段显式 set，避免 Bean 拷贝覆盖审计字段或逻辑删除语义。
        MediaFilePO mediaFile = new MediaFilePO();
        mediaFile.setOwnerId(ownerId);
        mediaFile.setBizType(bizType);
        mediaFile.setOriginalName(storedFile.originalName());
        mediaFile.setStoragePath(storedFile.storagePath());
        mediaFile.setUrl(storedFile.url());
        mediaFile.setContentType(storedFile.contentType());
        mediaFile.setFileSize(storedFile.fileSize());
        mediaFile.setFileHash(storedFile.fileHash());
        mediaFile.setUploadStatus(MediaUploadStatus.SUCCESS);
        mediaFile.setDeleted(0);
        mediaFileMapper.insert(mediaFile);
        return mediaFile;
    }

    /**
     * 查询当前用户可复用的成功媒资。
     *
     * @param ownerId 拥有者用户 ID
     * @param fileHash 文件摘要
     * @param bizType 业务类型
     * @return 可复用媒资；不存在返回 null
     */
    private MediaFilePO findReusableFile(Long ownerId, String fileHash, MediaBizType bizType) {
        return mediaFileMapper.selectOne(Wrappers.<MediaFilePO>lambdaQuery()
                .eq(MediaFilePO::getOwnerId, ownerId)
                .eq(MediaFilePO::getFileHash, fileHash)
                .eq(MediaFilePO::getBizType, bizType)
                .eq(MediaFilePO::getUploadStatus, MediaUploadStatus.SUCCESS)
                .last("LIMIT 1"));
    }

    /**
     * 按媒资类型补充业务子表记录。
     *
     * @param mediaFile 媒资主记录
     * @param bizType 业务类型
     */
    private void createBizRecordIfNeeded(MediaFilePO mediaFile, MediaBizType bizType) {
        // 课程视频和知识库文档后续会进入不同处理链路，因此上传成功时先建立各自子表记录。
        if (bizType == MediaBizType.COURSE_VIDEO) {
            MediaVideoPO video = new MediaVideoPO();
            video.setMediaFileId(mediaFile.getId());
            video.setDurationSeconds(0);
            video.setTranscodeStatus(MediaTranscodeStatus.SUCCESS);
            video.setDeleted(0);
            mediaVideoMapper.insert(video);
            return;
        }
        if (bizType == MediaBizType.KNOWLEDGE_DOC) {
            MediaDocumentPO document = new MediaDocumentPO();
            document.setMediaFileId(mediaFile.getId());
            document.setParseStatus(MediaParseStatus.PENDING);
            document.setDeleted(0);
            mediaDocumentMapper.insert(document);

            // day11.5 只发布待解析事件，真正解析、切片和向量化由 day12 之后接管。
            businessEventPublisher.publishMediaDocumentUploaded(
                    mediaFile.getId(),
                    document.getId(),
                    mediaFile.getOwnerId(),
                    mediaFile.getOriginalName(),
                    mediaFile.getUrl(),
                    mediaFile.getBizType());
        }
    }

    /**
     * 校验当前用户是否允许上传指定业务类型媒资。
     *
     * @param bizType 业务类型
     * @return 当前登录用户
     */
    private LoginUser requireUploadPermission(MediaBizType bizType) {
        LoginUser loginUser = userContextService.currentLoginUser();
        // 知识库文档本轮按 day06 合同允许登录用户上传，后续 day12 再收紧知识库管理权限。
        if (bizType == MediaBizType.KNOWLEDGE_DOC) {
            return loginUser;
        }
        if (isAdmin(loginUser)) {
            return loginUser;
        }
        if (hasRole(loginUser, MediaConstants.ROLE_TEACHER)
                && hasPermission(loginUser, MediaConstants.PERMISSION_COURSE_PUBLISH)) {
            return loginUser;
        }
        throw new BizException(MediaErrorCode.MEDIA_FORBIDDEN);
    }

    /**
     * 转换媒资列表项。
     *
     * @param mediaFile 媒资主记录
     * @return 对外返回的媒资信息
     */
    private MediaFileItem toMediaFileItem(MediaFilePO mediaFile) {
        return BeanCopyUtils.copyBean(mediaFile, MediaFileItem.class, (source, target) -> {
            // 保留旧字段别名，避免前端分阶段迁移时读取不到 fileId/fileUrl。
            target.setFileId(source.getId());
            target.setMediaId(source.getId());
            target.setFileName(source.getOriginalName());
            target.setFileUrl(source.getUrl());
        });
    }

    /**
     * 转换分片合并结果。
     *
     * @param mediaFile 媒资主记录
     * @return 合并结果
     */
    private MergeChunksResult toMergeResult(MediaFilePO mediaFile) {
        // 合并结果需要同时满足新合同字段和旧前端别名，字段较少且含派生值，保持显式赋值更清楚。
        MergeChunksResult result = MergeChunksResult.builder()
                .id(mediaFile.getId())
                .fileId(mediaFile.getId())
                .mediaId(mediaFile.getId())
                .originalName(mediaFile.getOriginalName())
                .fileName(mediaFile.getOriginalName())
                .url(mediaFile.getUrl())
                .fileUrl(mediaFile.getUrl())
                .contentType(mediaFile.getContentType())
                .fileSize(mediaFile.getFileSize())
                .fileHash(mediaFile.getFileHash())
                .bizType(mediaFile.getBizType())
                .uploadStatus(mediaFile.getUploadStatus())
                .createTime(mediaFile.getCreateTime())
                .build();
        if (mediaFile.getBizType() == MediaBizType.COURSE_VIDEO) {
            // 视频封面来自子表，不能依赖主表 Bean 拷贝。
            MediaVideoPO video = mediaVideoMapper.selectOne(Wrappers.<MediaVideoPO>lambdaQuery()
                    .eq(MediaVideoPO::getMediaFileId, mediaFile.getId())
                    .last("LIMIT 1"));
            if (video != null) {
                result.setCoverUrl(video.getCoverUrl());
            }
        }
        return result;
    }

    /**
     * 归一化文件摘要。
     *
     * @param fileHash 文件摘要
     * @return 去除首尾空白后的摘要
     */
    private String normalizeHash(String fileHash) {
        if (!StringUtils.hasText(fileHash)) {
            throw new BizException(MediaErrorCode.MEDIA_PARAM_INVALID, "文件摘要不能为空");
        }
        return fileHash.trim();
    }

    /**
     * 判断用户是否为管理员。
     *
     * @param loginUser 当前登录用户
     * @return 是管理员返回 true
     */
    private boolean isAdmin(LoginUser loginUser) {
        return hasRole(loginUser, MediaConstants.ROLE_ADMIN);
    }

    /**
     * 判断用户是否拥有指定角色。
     *
     * @param loginUser 当前登录用户
     * @param roleCode 角色编码
     * @return 拥有角色返回 true
     */
    private boolean hasRole(LoginUser loginUser, String roleCode) {
        List<String> roles = loginUser.getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role));
    }

    /**
     * 判断用户是否拥有指定权限点。
     *
     * @param loginUser 当前登录用户
     * @param permissionCode 权限编码
     * @return 拥有权限返回 true
     */
    private boolean hasPermission(LoginUser loginUser, String permissionCode) {
        List<String> permissions = loginUser.getPermissions();
        return permissions != null && permissions.contains(permissionCode);
    }
}
