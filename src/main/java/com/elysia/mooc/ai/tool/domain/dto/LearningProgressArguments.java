package com.elysia.mooc.ai.tool.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 学习进度工具参数。 */
@Data
public class LearningProgressArguments {

    /** 模型可能伪造的用户 ID，服务端只用于越权检测。 */
    @Min(value = 1, message = "用户ID必须为正数")
    private Long userId;

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    @Min(value = 1, message = "课程ID必须为正数")
    private Long courseId;
}
