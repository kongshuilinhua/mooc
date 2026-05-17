package com.elysia.mooc.ops.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/** 审计日志查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQuery extends PageQuery {

    /** 搜索关键字，匹配操作者、动作、目标、路径或 traceId。 */
    private String keyword;

    /** 操作动作编码。 */
    private String action;

    /** 操作人 ID。 */
    private Long operatorId;

    /** 目标类型。 */
    private String targetType;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 是否成功。 */
    private Boolean success;

    /** 开始时间。 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /** 结束时间。 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /** 排序字段，默认 createTime。 */
    private String sortBy;

    /** 是否升序，默认 false。 */
    private Boolean isAsc = false;
}
