package com.elysia.mooc.interaction.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建问题请求参数。 */
@Data
public class CreateQuestionRequest {

    /** 问题标题。 */
    @NotBlank(message = "问题标题不能为空")
    @Size(max = 128, message = "问题标题不能超过128个字符")
    private String title;

    /** 问题内容。 */
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 1000, message = "问题内容不能超过1000个字符")
    private String content;

    /** 小节 ID，可为空。 */
    @Positive(message = "小节ID必须为正数")
    private Long sectionId;

    /** 前端兼容字段，本轮不参与落库。 */
    @Positive(message = "视频时间点必须为正数")
    private Integer videoTimestamp;
}
