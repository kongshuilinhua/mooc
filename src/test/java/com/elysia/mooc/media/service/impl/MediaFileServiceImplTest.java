package com.elysia.mooc.media.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.auth.security.LoginUser;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.course.mapper.CourseSectionMapper;
import com.elysia.mooc.media.constants.MediaConstants;
import com.elysia.mooc.media.constants.MediaErrorCode;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import com.elysia.mooc.media.domain.po.MediaDocumentPO;
import com.elysia.mooc.media.domain.po.MediaFilePO;
import com.elysia.mooc.media.domain.vo.MediaFileItem;
import com.elysia.mooc.media.mapper.MediaDocumentMapper;
import com.elysia.mooc.media.mapper.MediaFileMapper;
import com.elysia.mooc.media.mapper.MediaVideoMapper;
import com.elysia.mooc.media.service.MediaStorageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

/** 媒资服务权限和删除边界测试。 */
@ExtendWith(MockitoExtension.class)
class MediaFileServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private MediaFileMapper mediaFileMapper;

    @Mock
    private MediaVideoMapper mediaVideoMapper;

    @Mock
    private MediaDocumentMapper mediaDocumentMapper;

    @Mock
    private CourseSectionMapper courseSectionMapper;

    @InjectMocks
    private MediaFileServiceImpl mediaFileService;

    @Test
    void uploadCourseVideoShouldRejectStudent() {
        when(userContextService.currentLoginUser()).thenReturn(loginUser(3L, List.of("STUDENT"), List.of()));
        MockMultipartFile file = new MockMultipartFile("file", "lesson.mp4", "video/mp4", "video".getBytes());

        assertThatThrownBy(() -> mediaFileService.uploadFile(file, MediaBizType.COURSE_VIDEO))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(MediaErrorCode.MEDIA_FORBIDDEN.code());
    }

    @Test
    void uploadKnowledgeDocShouldInsertMediaFile() {
        when(userContextService.currentLoginUser()).thenReturn(loginUser(8L, List.of("STUDENT"), List.of()));
        when(mediaStorageService.storeFile(any(), any()))
                .thenReturn(new MediaStorageService.StoredFile(
                        "doc.txt", "D:/mooc-storage/doc.txt", "/files/doc.txt", "text/plain", 3L, "hash-a"));
        when(mediaFileMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        MediaFileItem result = mediaFileService.uploadFile(
                new MockMultipartFile("file", "doc.txt", "text/plain", "doc".getBytes()),
                MediaBizType.KNOWLEDGE_DOC);

        assertThat(result.getFileName()).isEqualTo("doc.txt");
        assertThat(result.getFileUrl()).isEqualTo("/files/doc.txt");
        verify(mediaFileMapper).insert(any(MediaFilePO.class));
        verify(mediaDocumentMapper).insert(any(MediaDocumentPO.class));
    }

    @Test
    void deleteShouldRejectReferencedMedia() {
        when(userContextService.currentLoginUser())
                .thenReturn(loginUser(1L, List.of(MediaConstants.ROLE_ADMIN), List.of()));
        when(mediaFileMapper.selectById(20L)).thenReturn(mediaFile(20L, 2L));
        when(courseSectionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> mediaFileService.deleteFile(20L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(MediaErrorCode.MEDIA_IN_USE.code());
    }

    @Test
    void deleteShouldRejectOtherOwner() {
        when(userContextService.currentLoginUser()).thenReturn(loginUser(3L, List.of("TEACHER"), List.of()));
        when(mediaFileMapper.selectById(21L)).thenReturn(mediaFile(21L, 9L));

        assertThatThrownBy(() -> mediaFileService.deleteFile(21L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(MediaErrorCode.MEDIA_FORBIDDEN.code());
    }

    private LoginUser loginUser(Long userId, List<String> roles, List<String> permissions) {
        return new LoginUser(userId, "user" + userId, roles, permissions);
    }

    private MediaFilePO mediaFile(Long id, Long ownerId) {
        MediaFilePO mediaFile = new MediaFilePO();
        mediaFile.setId(id);
        mediaFile.setOwnerId(ownerId);
        mediaFile.setOriginalName("lesson.mp4");
        mediaFile.setUrl("/files/lesson.mp4");
        mediaFile.setContentType("video/mp4");
        mediaFile.setFileSize(5L);
        mediaFile.setFileHash("hash");
        mediaFile.setBizType(MediaBizType.COURSE_VIDEO);
        mediaFile.setUploadStatus(MediaUploadStatus.SUCCESS);
        return mediaFile;
    }
}
