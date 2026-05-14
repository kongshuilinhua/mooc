package com.elysia.mooc.media.service.impl;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.media.config.MediaStorageProperties;
import com.elysia.mooc.media.constants.MediaConstants;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.service.MediaStorageService;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 本地媒资存储服务实现。 */
@Service
@RequiredArgsConstructor
public class LocalMediaStorageServiceImpl implements MediaStorageService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MediaStorageProperties properties;

    /**
     * 保存普通上传文件。
     *
     * @param file    上传文件
     * @param bizType 业务类型
     * @return 已保存文件信息
     */
    @Override
    public StoredFile storeFile(MultipartFile file, MediaBizType bizType) {
        // 1. 后端必须再次校验大小和 MIME，不能只依赖前端上传控件限制。
        validateFile(file);
        String originalName = cleanFileName(file.getOriginalFilename());
        String fileHash = hash(file);
        String extension = extension(originalName);

        // 2. 路径使用业务类型、日期和摘要生成，不把用户文件名拼入物理路径，降低路径穿越和重名风险。
        String relativePath = bizType.getValue().toLowerCase(Locale.ROOT).replace('_', '/')
                + "/" + LocalDate.now().format(DATE_PATH_FORMATTER)
                + "/" + fileHash + extension;
        Path target = resolveStoragePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            // 3. 同 hash 物理文件已存在时直接复用，普通上传重试不会重复写磁盘。
            if (!Files.exists(target)) {
                file.transferTo(target);
            }
            return new StoredFile(
                    originalName,
                    target.toString(),
                    buildPublicUrl(relativePath),
                    safeContentType(file),
                    file.getSize(),
                    fileHash);
        } catch (Exception ex) {
            throw new BizException(MediaErrorCode.MEDIA_STORAGE_FAILED, "文件保存失败");
        }
    }

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
    @Override
    public StoredChunk storeChunk(
            MultipartFile file,
            String fileHash,
            Integer chunkIndex,
            Integer totalChunks,
            String fileName) {
        validateChunk(file, fileHash, chunkIndex, totalChunks, fileName);
        Path chunkPath = chunkPath(fileHash, chunkIndex);
        try {
            Files.createDirectories(chunkPath.getParent());
            // 分片按 hash 和序号幂等保存，前端重传同一分片时不覆盖已有内容。
            if (!Files.exists(chunkPath)) {
                file.transferTo(chunkPath);
            }
            return new StoredChunk(
                    cleanFileName(fileName),
                    safeContentType(file),
                    file.getSize(),
                    chunkIndex,
                    true);
        } catch (Exception ex) {
            throw new BizException(MediaErrorCode.MEDIA_STORAGE_FAILED, "分片保存失败");
        }
    }

    /**
     * 合并分片。
     *
     * @param fileHash    文件摘要
     * @param fileName    原文件名
     * @param totalChunks 分片总数
     * @param bizType     业务类型
     * @return 已保存文件信息
     */
    @Override
    public StoredFile mergeChunks(String fileHash, String fileName, Integer totalChunks, MediaBizType bizType) {
        if (!StringUtils.hasText(fileHash) || !StringUtils.hasText(fileName) || totalChunks == null || totalChunks <= 0) {
            throw new BizException(MediaErrorCode.MEDIA_PARAM_INVALID, "分片合并参数不正确");
        }
        String originalName = cleanFileName(fileName);
        String extension = extension(originalName);

        // 合并后的正式文件仍按摘要命名，保证重复合并和普通上传能落到同一物理文件。
        String relativePath = bizType.getValue().toLowerCase(Locale.ROOT).replace('_', '/')
                + "/" + LocalDate.now().format(DATE_PATH_FORMATTER)
                + "/" + fileHash + extension;
        Path target = resolveStoragePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            if (!Files.exists(target)) {
                // 只有目标文件不存在时检查分片完整性并写入，确保合并接口可安全重试。
                assertChunksComplete(fileHash, totalChunks);
                try (OutputStream outputStream = Files.newOutputStream(target)) {
                    for (int index = 0; index < totalChunks; index++) {
                        Files.copy(chunkPath(fileHash, index), outputStream);
                    }
                }
            }
            return new StoredFile(
                    originalName,
                    target.toString(),
                    buildPublicUrl(relativePath),
                    contentTypeFromName(originalName),
                    Files.size(target),
                    fileHash);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(MediaErrorCode.MEDIA_STORAGE_FAILED, "分片合并失败");
        }
    }

    /**
     * 校验普通文件基础约束。
     *
     * @param file 上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(MediaErrorCode.MEDIA_FILE_EMPTY);
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw new BizException(MediaErrorCode.MEDIA_SIZE_EXCEEDED);
        }
        if (!properties.getAllowedContentTypes().contains(safeContentType(file))) {
            throw new BizException(MediaErrorCode.MEDIA_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 校验分片参数。
     *
     * @param file 当前分片
     * @param fileHash 文件摘要
     * @param chunkIndex 分片序号
     * @param totalChunks 分片总数
     * @param fileName 原始文件名
     */
    private void validateChunk(
            MultipartFile file,
            String fileHash,
            Integer chunkIndex,
            Integer totalChunks,
            String fileName) {
        validateFile(file);
        if (!StringUtils.hasText(fileHash) || !StringUtils.hasText(fileName)
                || chunkIndex == null || totalChunks == null
                || chunkIndex < 0 || totalChunks <= 0 || chunkIndex >= totalChunks) {
            throw new BizException(MediaErrorCode.MEDIA_PARAM_INVALID, "分片参数不正确");
        }
    }

    /**
     * 检查待合并分片是否齐全。
     *
     * @param fileHash 文件摘要
     * @param totalChunks 分片总数
     */
    private void assertChunksComplete(String fileHash, Integer totalChunks) {
        for (int index = 0; index < totalChunks; index++) {
            if (!Files.exists(chunkPath(fileHash, index))) {
                throw new BizException(MediaErrorCode.MEDIA_CHUNK_INCOMPLETE);
            }
        }
    }

    /**
     * 计算上传文件 SHA-256 摘要。
     *
     * @param file 上传文件
     * @return 十六进制文件摘要
     */
    private String hash(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(MediaConstants.HASH_ALGORITHM);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前运行环境不支持SHA-256算法", ex);
        } catch (Exception ex) {
            throw new BizException(MediaErrorCode.MEDIA_STORAGE_FAILED, "文件摘要计算失败");
        }
    }

    /**
     * 生成分片临时路径。
     *
     * @param fileHash 文件摘要
     * @param chunkIndex 分片序号
     * @return 分片路径
     */
    private Path chunkPath(String fileHash, Integer chunkIndex) {
        // 分片目录只允许安全字符，避免摘要字段被构造成跨目录路径。
        String safeHash = fileHash.replaceAll("[^a-zA-Z0-9._-]", "");
        return rootPath().resolve(MediaConstants.CHUNK_DIR).resolve(safeHash).resolve(chunkIndex + ".part").normalize();
    }

    /**
     * 解析正式文件存储路径。
     *
     * @param relativePath 相对存储路径
     * @return 归一化后的绝对路径
     */
    private Path resolveStoragePath(String relativePath) {
        Path root = rootPath();
        Path target = root.resolve(relativePath).normalize();
        // 归一化后仍必须在根目录下，避免通过 ../ 越权写入任意位置。
        if (!target.startsWith(root)) {
            throw new BizException(MediaErrorCode.MEDIA_PARAM_INVALID, "文件路径不合法");
        }
        return target;
    }

    /**
     * 获取本地存储根路径。
     *
     * @return 归一化后的绝对根路径
     */
    private Path rootPath() {
        return Path.of(properties.getRootPath()).toAbsolutePath().normalize();
    }

    /**
     * 构建公开访问地址。
     *
     * @param relativePath 相对存储路径
     * @return 公开访问 URL
     */
    private String buildPublicUrl(String relativePath) {
        String prefix = properties.getPublicPrefix();
        String normalizedPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return normalizedPrefix + "/" + relativePath.replace("\\", "/");
    }

    /**
     * 获取上传文件 MIME 类型。
     *
     * @param file 上传文件
     * @return MIME 类型；缺失时返回通用二进制类型
     */
    private String safeContentType(MultipartFile file) {
        return StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
    }

    /**
     * 根据文件名探测内容类型。
     *
     * @param fileName 文件名
     * @return MIME 类型；探测失败时返回通用二进制类型
     */
    private String contentTypeFromName(String fileName) {
        try {
            String contentType = Files.probeContentType(Path.of(fileName));
            return StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        } catch (Exception ex) {
            return "application/octet-stream";
        }
    }

    /**
     * 清洗展示用原始文件名。
     *
     * @param originalName 原始文件名
     * @return 安全文件名
     */
    private String cleanFileName(String originalName) {
        String name = StringUtils.hasText(originalName) ? Path.of(originalName).getFileName().toString() : "upload.bin";
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (!StringUtils.hasText(cleaned)) {
            return UUID.randomUUID() + ".bin";
        }
        return cleaned.length() > 255 ? cleaned.substring(cleaned.length() - 255) : cleaned;
    }

    /**
     * 提取文件扩展名。
     *
     * @param fileName 文件名
     * @return 小写扩展名；没有扩展名时返回空字符串
     */
    private String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }
}
