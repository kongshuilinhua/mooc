package com.elysia.mooc.exam.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建题目选项请求。 */
@Data
public class CreateQuestionOptionRequest {

    /** 选项编码，例如 A/B/C/D。 */
    @NotBlank(message = "选项编码不能为空")
    @Size(max = 8, message = "选项编码不能超过8个字符")
    private String optionKey;

    /** 选项内容。 */
    @NotBlank(message = "选项内容不能为空")
    @Size(max = 1000, message = "选项内容不能超过1000个字符")
    private String optionText;

    /** 是否正确。 */
    @NotNull(message = "选项是否正确不能为空")
    private Boolean correct;

    /** 排序值。 */
    private Integer sort;
}
