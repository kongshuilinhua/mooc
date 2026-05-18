package com.elysia.mooc.studyarchive.domain.vo;

import java.time.LocalDate;
import lombok.Data;

/** 学习日报响应。 */
@Data
public class LearningReportVO {

    /** 报告日期。 */
    private LocalDate reportDate;

    /** 学习分钟数。 */
    private Integer studyMinutes;

    /** 兼容前端旧字段，语义等同 studyMinutes。 */
    private Integer learningMinutes;

    /** 完成小节数。 */
    private Integer completedSections;

    /** 错题数。 */
    private Integer wrongCount;

    /** AI 提问次数。 */
    private Integer aiAskCount;

    /** 报告来源，PERSISTED 表示来自 learning_report，AGGREGATED 表示轻量聚合。 */
    private String source;
}
