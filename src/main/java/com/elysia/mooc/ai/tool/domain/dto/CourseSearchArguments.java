package com.elysia.mooc.ai.tool.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 课程搜索工具参数。 */
@Data
public class CourseSearchArguments {

    /** 搜索关键字，可为空，为空时返回热门已发布课程。 */
    @Size(max = 100, message = "课程搜索关键字不能超过100个字符")
    private String keyword;

    /** 返回数量。 */
    @Min(value = 1, message = "课程搜索数量不能小于1")
    @Max(value = 10, message = "课程搜索数量不能大于10")
    private Integer limit;
}
