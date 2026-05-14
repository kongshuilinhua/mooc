package com.elysia.mooc.interaction.domain.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 采纳回答请求参数。 */
@Data
public class AcceptAnswerRequest {

    /** 前端兼容字段，实际以路径中的 answerId 为准。 */
    @Positive(message = "问题ID必须为正数")
    private Long questionId;
}
