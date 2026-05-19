package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.ops.domain.enums.OpsReviewPriority;
import com.elysia.mooc.ops.domain.enums.OpsReviewStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 审核任务创建结果。 */
@Data
public class ReviewTaskResultVO {

    /** 审核任务 ID。 */
    private Long reviewId;

    /** 业务类型。 */
    private String targetType;

    /** 业务 ID。 */
    private String targetId;

    /** 审核状态。 */
    private OpsReviewStatus status;

    /** 审核说明。 */
    private String reason;

    /** 优先级。 */
    private OpsReviewPriority priority;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
