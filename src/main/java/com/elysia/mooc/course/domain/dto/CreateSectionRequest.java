package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建小节请求参数。 */
@Data
public class CreateSectionRequest implements Checker {

    /** 小节标题。 */
    @NotBlank(message = "小节标题不能为空")
    @Size(max = 128, message = "小节标题不能超过128个字符")
    private String title;

    /** 媒资 ID。 */
    private Long mediaId;

    /** 视频时长，单位秒。 */
    private Integer durationSeconds;

    /** 是否可试看。 */
    private Boolean freePreview;

    /** 小节排序。 */
    @NotNull(message = "小节排序不能为空")
    private Integer sort;

    @Override
    public void check() {
        this.title = title == null ? null : title.trim();
        this.durationSeconds = durationSeconds == null ? 0 : durationSeconds;
        this.freePreview = freePreview != null && freePreview;
        if (mediaId != null && mediaId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "媒资ID必须为正数");
        }
        if (durationSeconds < 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "视频时长不能小于0");
        }
        if (sort != null && sort <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "小节排序必须为正整数");
        }
    }
}
