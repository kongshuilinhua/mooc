package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建章节请求参数。 */
@Data
public class CreateChapterRequest implements Checker {

    /** 章节标题。 */
    @NotBlank(message = "章节标题不能为空")
    @Size(max = 128, message = "章节标题不能超过128个字符")
    private String title;

    /** 章节简介。 */
    @Size(max = 500, message = "章节简介不能超过500个字符")
    private String summary;

    /** 章节排序。 */
    @NotNull(message = "章节排序不能为空")
    private Integer sort;

    @Override
    public void check() {
        this.title = title == null ? null : title.trim();
        this.summary = StringUtils.hasText(summary) ? summary.trim() : null;
        if (sort != null && sort <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "章节排序必须为正整数");
        }
    }
}
