package com.elysia.mooc.interaction.domain.vo;

import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** 管理端举报列表项。 */
@Data
@Builder
public class ReportItemVO {

    /** 举报 ID。 */
    private Long id;

    /** 目标类型。 */
    private InteractionTargetType targetType;

    /** 目标 ID。 */
    private Long targetId;

    /** 举报人 ID。 */
    private Long reporterId;

    /** 举报原因。 */
    private String reason;

    /** 举报详情。 */
    private String detail;

    /** 举报状态。 */
    private ReportStatus status;

    /** 处理人 ID。 */
    private Long handlerId;

    /** 处理结果。 */
    private String handleResult;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
