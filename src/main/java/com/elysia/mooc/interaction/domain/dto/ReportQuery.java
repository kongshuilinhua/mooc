package com.elysia.mooc.interaction.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 管理端举报分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQuery extends PageQuery {

    /** 处理状态。 */
    private ReportStatus status;

    /** 举报目标类型。 */
    private InteractionTargetType targetType;
}
