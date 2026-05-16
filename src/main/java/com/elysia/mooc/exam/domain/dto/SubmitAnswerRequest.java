package com.elysia.mooc.exam.domain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 提交单题答案请求。 */
@Data
public class SubmitAnswerRequest {

    /** 题目 ID。 */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 作答内容，兼容 answerContent 字段。 */
    @JsonAlias("answerContent")
    @Size(max = 4000, message = "作答内容不能超过4000个字符")
    private String answer;
}
