package com.elysia.mooc.knowledge.domain.dto;

import com.elysia.mooc.common.enums.EnableStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 修改知识库请求。 */
@Data
public class UpdateKnowledgeBaseRequest {

    /** 知识库名称。 */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 128, message = "知识库名称不能超过128个字符")
    private String name;

    /** 知识库说明。 */
    @Size(max = 500, message = "知识库说明不能超过500个字符")
    private String description;

    /** 是否启用，兼容前端当前表单。 */
    private Boolean enabled;

    /** 启停状态，兼容状态枚举输入。 */
    private EnableStatus status;
}
