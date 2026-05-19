package com.elysia.mooc.ai.generator.domain.dto;

import com.elysia.mooc.ai.generator.constants.AiGeneratorConstants;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 生成章节总结请求。 */
@Data
public class GenerateChapterSummaryRequest {

    /** 总结风格。 */
    @Size(max = 32, message = "总结风格不能超过32个字符")
    private String style = AiGeneratorConstants.DEFAULT_SUMMARY_STYLE;

    /** 输出长度：SHORT、MEDIUM、LONG。 */
    @Size(max = 16, message = "输出长度不能超过16个字符")
    private String length = AiGeneratorConstants.DEFAULT_SUMMARY_LENGTH;
}
