package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.RatingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 课程评价结果。 */
@Data
@Builder
public class RatingResultVO {

    /** 评价 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 评分。 */
    private Integer score;

    /** 评价内容。 */
    private String content;

    /** 评价状态。 */
    private RatingStatus status;

    /** 课程最新平均分。 */
    private BigDecimal ratingScore;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
