package com.elysia.mooc.ai.generator.domain.vo;

import com.elysia.mooc.ai.generator.domain.enums.AiLearningPathStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** AI 学习路径生成结果。 */
@Data
public class LearningPathResultVO {

    /** 学习路径 ID。 */
    private Long pathId;

    /** 学生 ID。 */
    private Long studentId;

    /** 学习主题。 */
    private String theme;

    /** 路径阶段。 */
    private List<LearningPathStageVO> stages;

    /** 每日建议学习分钟数。 */
    private Integer dailyMinutes;

    /** 失效时间。 */
    private LocalDateTime expireTime;

    /** 路径状态。 */
    private AiLearningPathStatus status;

    /** 生成来源，模型或规则兜底。 */
    private String generationSource;

    /** 错误信息。 */
    private String errorMessage;
}
