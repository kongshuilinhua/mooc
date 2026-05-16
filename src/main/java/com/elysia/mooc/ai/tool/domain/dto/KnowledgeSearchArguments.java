package com.elysia.mooc.ai.tool.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 知识库检索工具参数。 */
@Data
public class KnowledgeSearchArguments {

    /** 检索文本。 */
    @NotBlank(message = "知识库检索文本不能为空")
    @Size(max = 500, message = "知识库检索文本不能超过500个字符")
    private String query;

    /** 知识库 ID。 */
    @Min(value = 1, message = "知识库ID必须为正数")
    private Long knowledgeBaseId;

    /** 课程 ID。 */
    @Min(value = 1, message = "课程ID必须为正数")
    private Long courseId;

    /** 检索数量。 */
    @Min(value = 1, message = "检索数量不能小于1")
    @Max(value = 10, message = "检索数量不能大于10")
    private Integer topK;
}
