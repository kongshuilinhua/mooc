package com.elysia.mooc.ai.tool.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 最近学习工具参数。 */
@Data
public class RecentLearningArguments {

    /** 返回数量。 */
    @Min(value = 1, message = "最近学习数量不能小于1")
    @Max(value = 10, message = "最近学习数量不能大于10")
    private Integer limit;
}
