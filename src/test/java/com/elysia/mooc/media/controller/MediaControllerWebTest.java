package com.elysia.mooc.media.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.exception.GlobalExceptionHandler;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.dto.MediaFileQuery;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import com.elysia.mooc.media.domain.vo.MediaFileItem;
import com.elysia.mooc.media.domain.vo.UploadChunkResult;
import com.elysia.mooc.media.service.MediaFileService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 媒资控制层 HTTP 合同测试。 */
@ExtendWith(MockitoExtension.class)
class MediaControllerWebTest {

    @Mock
    private MediaFileService mediaFileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverterFactory(new StringToBaseEnumConverterFactory());
        mockMvc = MockMvcBuilders.standaloneSetup(new MediaController(mediaFileService))
                .setConversionService(conversionService)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadFileShouldAcceptLowercaseBizType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", "cover".getBytes());
        when(mediaFileService.uploadFile(any(), eq(MediaBizType.COURSE_COVER)))
                .thenReturn(MediaFileItem.builder()
                        .id(10L)
                        .fileId(10L)
                        .mediaId(10L)
                        .fileName("cover.png")
                        .fileUrl("/files/course/cover/cover.png")
                        .contentType("image/png")
                        .fileSize(5L)
                        .bizType(MediaBizType.COURSE_COVER)
                        .uploadStatus(MediaUploadStatus.SUCCESS)
                        .build());

        mockMvc.perform(multipart("/api/media/files")
                        .file(file)
                        .param("bizType", "course_cover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileId").value(10))
                .andExpect(jsonPath("$.data.fileUrl").value("/files/course/cover/cover.png"))
                .andExpect(jsonPath("$.data.bizType").value("COURSE_COVER"));
    }

    @Test
    void uploadChunkShouldAcceptFileMd5Alias() throws Exception {
        MockMultipartFile chunk = new MockMultipartFile(
                "file", "chunk.part", "text/plain", "part".getBytes());
        when(mediaFileService.uploadChunk(any(), eq("abc123"), eq(0), eq(2), eq("lesson.txt"),
                eq(MediaBizType.KNOWLEDGE_DOC)))
                .thenReturn(UploadChunkResult.builder()
                        .fileName("lesson.txt")
                        .fileSize(4L)
                        .contentType("text/plain")
                        .uploaded(true)
                        .chunkIndex(0)
                        .build());

        mockMvc.perform(multipart("/api/media/chunks")
                        .file(chunk)
                        .param("fileMd5", "abc123")
                        .param("chunkIndex", "0")
                        .param("totalChunks", "2")
                        .param("fileName", "lesson.txt")
                        .param("bizType", "knowledge_doc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploaded").value(true))
                .andExpect(jsonPath("$.data.chunkIndex").value(0));
    }

    @Test
    void listFilesShouldReturnPageResultAndAcceptStatusAlias() throws Exception {
        when(mediaFileService.listFiles(any(MediaFileQuery.class)))
                .thenReturn(PageResult.of(1L, 10, List.of(MediaFileItem.builder()
                        .fileId(11L)
                        .fileName("lesson.mp4")
                        .bizType(MediaBizType.COURSE_VIDEO)
                        .uploadStatus(MediaUploadStatus.SUCCESS)
                        .build())));

        mockMvc.perform(get("/api/media/files")
                        .param("pageNo", "1")
                        .param("pageSize", "10")
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.totalPage").value(1))
                .andExpect(jsonPath("$.data.list[0].fileId").value(11));

        ArgumentCaptor<MediaFileQuery> captor = ArgumentCaptor.forClass(MediaFileQuery.class);
        verify(mediaFileService).listFiles(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getStatus())
                .isEqualTo(MediaUploadStatus.SUCCESS);
    }

    @Test
    void deleteFileShouldReturn403WhenServiceRejects() throws Exception {
        when(mediaFileService.deleteFile(12L)).thenThrow(new BizException(MediaErrorCode.MEDIA_FORBIDDEN));

        mockMvc.perform(delete("/api/media/files/12"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(MediaErrorCode.MEDIA_FORBIDDEN.code()))
                .andExpect(jsonPath("$.message").value(MediaErrorCode.MEDIA_FORBIDDEN.message()));
    }
}
