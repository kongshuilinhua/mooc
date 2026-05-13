package com.elysia.mooc.media.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 分片上传结果。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadChunkResult {

    /** 文件 ID，分片阶段未合并时为空。 */
    private Long fileId;

    /** 原始文件名。 */
    private String fileName;

    /** 文件访问地址，分片阶段未合并时为空。 */
    private String fileUrl;

    /** 当前分片大小。 */
    private Long fileSize;

    /** 内容类型。 */
    private String contentType;

    /** 当前分片是否已上传。 */
    private Boolean uploaded;

    /** 分片序号。 */
    private Integer chunkIndex;
}
