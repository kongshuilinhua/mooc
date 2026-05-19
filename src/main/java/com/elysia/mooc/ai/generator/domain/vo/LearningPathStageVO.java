package com.elysia.mooc.ai.generator.domain.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** 学习路径阶段。 */
@Data
@Builder
public class LearningPathStageVO {

    /** 阶段名称。 */
    private String stageName;

    /** 阶段目标。 */
    private String goal;

    /** 阶段天数。 */
    private Integer durationDays;

    /** 学习任务。 */
    private List<String> tasks;
}
