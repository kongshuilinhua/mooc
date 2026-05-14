package com.elysia.mooc.interaction.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建回答请求参数。 */
@Data
public class CreateAnswerRequest {

    /** 回答内容。 */
    @NotBlank(message = "回答内容不能为空")
    @Size(max = 1000, message = "回答内容不能超过1000个字符")
    private String content;
}
