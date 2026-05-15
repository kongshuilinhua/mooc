package com.elysia.mooc.knowledge.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 向量检索调试请求。 */
@Data
public class VectorSearchRequest {

    /** 检索文本。 */
    @NotBlank(message = "检索文本不能为空")
    private String query;

    /** 知识库 ID，可为空。 */
    @Min(value = 1, message = "知识库ID必须为正数")
    private Long knowledgeBaseId;

    /** 返回数量。 */
    @Min(value = 1, message = "检索数量不能小于1")
    @Max(value = 20, message = "检索数量不能大于20")
    private Integer topK = 5;
}
