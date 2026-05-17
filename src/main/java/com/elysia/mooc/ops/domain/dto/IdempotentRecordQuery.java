package com.elysia.mooc.ops.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/** 幂等记录查询条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdempotentRecordQuery extends PageQuery {

    /** 搜索关键字，匹配幂等键、业务类型、业务 ID 或请求摘要。 */
    private String keyword;

    /** 业务类型。 */
    private String bizType;

    /** 业务 ID。 */
    private String bizId;

    /** 幂等处理状态。 */
    private IdempotentStatus status;

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
