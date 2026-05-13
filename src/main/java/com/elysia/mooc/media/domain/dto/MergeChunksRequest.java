package com.elysia.mooc.media.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 合并分片请求参数。 */
@Data
public class MergeChunksRequest implements Checker {

    /** 文件摘要，正式口径。 */
    private String fileHash;

    /** 文件 MD5，兼容前端旧字段。 */
    private String fileMd5;

    /** 原始文件名。 */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;

    /** 分片总数。 */
    @NotNull(message = "分片总数不能为空")
    private Integer totalChunks;

    /** 业务类型。 */
    @NotNull(message = "业务类型不能为空")
    private MediaBizType bizType;

    @Override
    public void check() {
        this.fileHash = normalizeHash(fileHash);
        this.fileMd5 = normalizeHash(fileMd5);
        this.fileName = fileName == null ? null : fileName.trim();
        if (fileHash == null && fileMd5 == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "文件摘要不能为空");
        }
        if (totalChunks != null && totalChunks <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "分片总数必须为正整数");
        }
    }

    /** 获取兼容后的文件摘要。 */
    public String resolvedFileHash() {
        return fileHash == null ? fileMd5 : fileHash;
    }

    private String normalizeHash(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
