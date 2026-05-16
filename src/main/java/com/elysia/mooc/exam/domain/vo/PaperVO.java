package com.elysia.mooc.exam.domain.vo;

import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 试卷响应。 */
@Data
public class PaperVO {

    /** 试卷 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 试卷标题。 */
    private String title;

    /** 试卷描述。 */
    private String description;

    /** 总分。 */
    private BigDecimal totalScore;

    /** 及格分。 */
    private BigDecimal passScore;

    /** 考试时长，单位分钟。 */
    private Integer durationMinutes;

    /** 试卷状态。 */
    private ExamPaperStatus status;

    /** 题目列表。 */
    private List<PaperQuestionVO> questions;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
