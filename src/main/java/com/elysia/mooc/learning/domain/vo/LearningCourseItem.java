package com.elysia.mooc.learning.domain.vo;

import com.elysia.mooc.learning.domain.enums.LearningCourseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 我的课程列表项。 */
@Data
@Builder
public class LearningCourseItem {

    /** 课程 ID。 */
    private Long courseId;

    /** 课程名称。 */
    private String courseName;

    /** 课程封面。 */
    private String coverUrl;

    /** 学习进度百分比。 */
    private BigDecimal progressPercent;

    /** 累计学习秒数。 */
    private Integer learnedSeconds;

    /** 最近学习小节 ID，兼容前端旧 videoId 口径。 */
    private Long lastVideoId;

    /** 最近学习小节标题。 */
    private String lastSectionTitle;

    /** 最近学习时间。 */
    private LocalDateTime lastLearnTime;

    /** 学习状态。 */
    private LearningCourseStatus status;
}
