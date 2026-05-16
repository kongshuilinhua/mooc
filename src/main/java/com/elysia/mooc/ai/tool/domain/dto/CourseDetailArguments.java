package com.elysia.mooc.ai.tool.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 课程详情工具参数。 */
@Data
public class CourseDetailArguments {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须为正数")
    private Long courseId;
}
