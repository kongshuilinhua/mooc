package com.elysia.mooc.interaction.domain.dto;

import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建举报请求参数。 */
@Data
public class ReportRequest {

    /** 举报目标类型。 */
    @NotNull(message = "举报对象类型不能为空")
    private InteractionTargetType targetType;

    /** 举报目标 ID。 */
    @NotNull(message = "举报对象ID不能为空")
    @Positive(message = "举报对象ID必须为正数")
    private Long targetId;

    /** 举报原因。 */
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 255, message = "举报原因不能超过255个字符")
    private String reason;

    /** 举报详情。 */
    @Size(max = 1000, message = "举报详情不能超过1000个字符")
    private String detail;

    /** 前端兼容字段，落库优先级低于 detail。 */
    @Size(max = 1000, message = "举报详情不能超过1000个字符")
    private String content;
}
