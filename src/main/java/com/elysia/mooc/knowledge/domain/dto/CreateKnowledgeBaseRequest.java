package com.elysia.mooc.knowledge.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建知识库请求。 */
@Data
public class CreateKnowledgeBaseRequest {

    /** 知识库名称。 */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过128个字符")
    private String name;

    /** 知识库编码；不传时由后端按名称和课程生成。 */
    @Size(max = 64, message = "知识库编码不能超过64个字符")
    private String code;

    /** 知识库说明。 */
    @Size(max = 500, message = "知识库说明不能超过500个字符")
    private String description;

    /** 绑定课程 ID；为空时创建平台知识库。 */
    private Long courseId;
}
