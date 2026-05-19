package com.elysia.mooc.ai.generator.domain.vo;

import com.elysia.mooc.ai.generator.domain.enums.AiGenerationStatus;
import java.util.List;
import lombok.Data;

/** 章节总结生成结果。 */
@Data
public class ChapterSummaryResultVO {

    /** 生成任务 ID。 */
    private Long taskId;

    /** 章节 ID。 */
    private Long chapterId;

    /** 总结正文。 */
    private String summary;

    /** 关键点。 */
    private List<String> keyPoints;

    /** 引用来源。 */
    private List<GenerationSourceVO> sources;

    /** 任务状态。 */
    private AiGenerationStatus status;

    /** 生成来源，模型或规则兜底。 */
    private String generationSource;

    /** 错误信息。 */
    private String errorMessage;
}
