package com.elysia.mooc.interaction.domain.dto;

import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理端处理举报请求参数。 */
@Data
public class HandleReportRequest {

    /** 处理后的举报状态。 */
    @NotNull(message = "举报处理状态不能为空")
    private ReportStatus status;

    /** 处理结果。 */
    @Size(max = 1000, message = "处理结果不能超过1000个字符")
    private String handleResult;

    /** 前端兼容字段，落库优先级低于 handleResult。 */
    @Size(max = 1000, message = "处理备注不能超过1000个字符")
    private String remark;
}
