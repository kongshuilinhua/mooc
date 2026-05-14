package com.elysia.mooc.course.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 提交课程审核请求参数。 */
@Data
public class SubmitCourseAuditRequest {

    /** 提交审核说明。 */
    @Size(max = 1000, message = "提交说明不能超过1000个字符")
    private String remark;
}
