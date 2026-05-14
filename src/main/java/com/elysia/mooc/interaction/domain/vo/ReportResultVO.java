package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 举报创建结果。 */
@Data
@Builder
public class ReportResultVO {

    /** 举报 ID。 */
    private Long id;

    /** 目标类型。 */
    private InteractionTargetType targetType;

    /** 目标 ID。 */
    private Long targetId;

    /** 举报状态。 */
    private ReportStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
