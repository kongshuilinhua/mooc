package com.elysia.mooc.media.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.media.domain.enums.MediaBizType;
import com.elysia.mooc.media.domain.enums.MediaUploadStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 媒资文件分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MediaFileQuery extends PageQuery {

    /** 业务类型。 */
    private MediaBizType bizType;

    /** 上传状态。 */
    private MediaUploadStatus uploadStatus;

    /** 搜索关键字。 */
    @Size(max = 100, message = "搜索关键字不能超过100个字符")
    private String keyword;
}
