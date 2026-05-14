package com.elysia.mooc.media.domain.vo;

import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 媒资文件列表项和上传结果。 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileItem {

    /** 媒资 ID。 */
    private Long id;

    /** 兼容前端旧字段：媒资 ID。 */
    private Long fileId;

    /** 兼容前端旧字段：媒资 ID。 */
    private Long mediaId;

    /** 原始文件名。 */
    private String originalName;

    /** 兼容前端旧字段：文件名。 */
    private String fileName;

    /** 访问地址。 */
    private String url;

    /** 兼容前端旧字段：访问地址。 */
    private String fileUrl;

    /** 内容类型。 */
    private String contentType;

    /** 文件大小。 */
    private Long fileSize;

    /** 文件摘要。 */
    private String fileHash;

    /** 业务类型。 */
    private MediaBizType bizType;

    /** 上传状态。 */
    private MediaUploadStatus uploadStatus;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
