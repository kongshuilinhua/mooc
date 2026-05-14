package com.elysia.mooc.course.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 课程下架请求参数。 */
@Data
public class OfflineCourseRequest {

    /** 下架原因，必须填写。 */
    @NotBlank(message = "下架原因不能为空")
    @Size(max = 1000, message = "下架原因不能超过1000个字符")
    private String reason;
}
