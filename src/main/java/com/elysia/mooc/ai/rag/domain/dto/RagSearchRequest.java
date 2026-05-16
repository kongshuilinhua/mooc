package com.elysia.mooc.ai.rag.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** RAG 检索预览请求。 */
@Data
public class RagSearchRequest {

    /** 检索问题或关键词。 */
    @NotBlank(message = "检索文本不能为空")
    private String query;

    /** 指定知识库 ID，可为空。 */
    @Min(value = 1, message = "知识库ID必须为正数")
    private Long knowledgeBaseId;

    /** 课程上下文 ID，可为空。 */
    @Min(value = 1, message = "课程ID必须为正数")
    private Long courseId;

    /** 检索数量。 */
    @Min(value = 1, message = "检索数量不能小于1")
    @Max(value = 20, message = "检索数量不能大于20")
    private Integer topK;
}
