package com.elysia.mooc.learning.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 加入课程请求。 */
@Data
public class JoinCourseRequest {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    private Long courseId;
}
