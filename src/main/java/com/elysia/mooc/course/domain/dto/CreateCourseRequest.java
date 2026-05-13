package com.elysia.mooc.course.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.course.constants.CourseConstants;
import com.elysia.mooc.course.domain.enums.CourseDifficulty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 创建课程请求参数。 */
@Data
public class CreateCourseRequest implements Checker {

    /** 课程标题。 */
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 128, message = "课程标题不能超过128个字符")
    private String title;

    /** 课程简介。 */
    @NotBlank(message = "课程简介不能为空")
    @Size(max = 500, message = "课程简介不能超过500个字符")
    private String summary;

    /** 课程详情。 */
    @Size(max = 10000, message = "课程详情不能超过10000个字符")
    private String description;

    /** 课程封面地址。 */
    @Size(max = 500, message = "课程封面地址不能超过500个字符")
    private String coverUrl;

    /** 分类 ID。 */
    @NotNull(message = "课程分类不能为空")
    private Long categoryId;

    /** 课程标签 ID 列表。 */
    private List<Long> tagIds;

    /** 课程难度。 */
    @NotNull(message = "课程难度不能为空")
    private CourseDifficulty difficulty;

    /** 课程价格。 */
    @DecimalMin(value = "0.00", message = "课程价格不能小于0")
    @Digits(integer = 8, fraction = 2, message = "课程价格最多保留两位小数")
    private BigDecimal price;

    @Override
    public void check() {
        this.title = normalizeText(title);
        this.summary = normalizeText(summary);
        this.description = normalizeOptionalText(description);
        this.coverUrl = normalizeOptionalText(coverUrl);
        this.price = price == null ? BigDecimal.ZERO : price;
        this.tagIds = normalizeTagIds(tagIds);
        if (categoryId != null && categoryId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程分类ID必须为正数");
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<Long> normalizeTagIds(List<Long> values) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        List<Long> normalized = values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalized.size() > CourseConstants.MAX_TAG_BIND_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程标签最多选择10个");
        }
        if (normalized.stream().anyMatch(tagId -> tagId <= 0)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程标签ID必须为正数");
        }
        return normalized;
    }
}
