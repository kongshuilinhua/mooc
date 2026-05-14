package com.elysia.mooc.media.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.media.config.MediaStorageProperties;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.service.MediaStorageService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/** 本地媒资存储测试。 */
class LocalMediaStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void storeFileShouldPersistAndBuildPublicUrl() {
        LocalMediaStorageServiceImpl storageService = storageService();
        MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", "hello".getBytes());

        MediaStorageService.StoredFile storedFile = storageService.storeFile(file, MediaBizType.KNOWLEDGE_DOC);

        assertThat(storedFile.originalName()).isEqualTo("doc.txt");
        assertThat(storedFile.url()).startsWith("/files/knowledge/doc/");
        assertThat(Files.exists(Path.of(storedFile.storagePath()))).isTrue();
        assertThat(storedFile.fileHash()).hasSize(64);
    }

    @Test
    void storeFileShouldRejectIllegalContentType() {
        LocalMediaStorageServiceImpl storageService = storageService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "bad".getBytes());

        assertThatThrownBy(() -> storageService.storeFile(file, MediaBizType.KNOWLEDGE_DOC))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(MediaErrorCode.MEDIA_TYPE_NOT_ALLOWED.code());
    }

    @Test
    void chunkMergeShouldBeIdempotent() {
        LocalMediaStorageServiceImpl storageService = storageService();
        storageService.storeChunk(new MockMultipartFile("file", "a.part", "text/plain", "he".getBytes()),
                "hash-1", 0, 2, "lesson.txt");
        storageService.storeChunk(new MockMultipartFile("file", "b.part", "text/plain", "llo".getBytes()),
                "hash-1", 1, 2, "lesson.txt");

        MediaStorageService.StoredFile first = storageService.mergeChunks(
                "hash-1", "lesson.txt", 2, MediaBizType.KNOWLEDGE_DOC);
        MediaStorageService.StoredFile second = storageService.mergeChunks(
                "hash-1", "lesson.txt", 2, MediaBizType.KNOWLEDGE_DOC);

        assertThat(first.storagePath()).isEqualTo(second.storagePath());
        assertThat(first.fileSize()).isEqualTo(5L);
        assertThat(first.url()).startsWith("/files/knowledge/doc/");
    }

    private LocalMediaStorageServiceImpl storageService() {
        MediaStorageProperties properties = new MediaStorageProperties();
        properties.setRootPath(tempDir.toString());
        properties.setPublicPrefix("/files");
        properties.setMaxFileSize(1024L);
        properties.setAllowedContentTypes(List.of("text/plain", "image/png"));
        return new LocalMediaStorageServiceImpl(properties);
    }
}
