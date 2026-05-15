package com.elysia.mooc.event.domain.payload;

import com.elysia.mooc.media.domain.enums.MediaBizType;

/** 知识库文档上传事件载荷。 */
public record MediaDocumentUploadedPayload(
        Long mediaFileId,
        Long documentId,
        Long ownerId,
        String originalName,
        String fileUrl,
        MediaBizType bizType) {
}
