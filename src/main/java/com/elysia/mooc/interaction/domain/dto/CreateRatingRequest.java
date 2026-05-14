package com.elysia.mooc.interaction.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建或更新课程评价请求参数。 */
@Data
public class CreateRatingRequest {

    /** 课程评分。 */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 5, message = "评分不能大于5")
    private Integer score;

    /** 前端评价内容字段，落库到 comment。 */
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String content;

    /** 后端兼容字段，优先级低于 content。 */
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String comment;
}
