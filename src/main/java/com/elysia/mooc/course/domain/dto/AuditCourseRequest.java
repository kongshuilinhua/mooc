package com.elysia.mooc.course.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 审核通过请求参数。 */
@Data
public class AuditCourseRequest {

    /** 审核通过说明。 */
    @Size(max = 1000, message = "审核说明不能超过1000个字符")
    private String remark;
}
