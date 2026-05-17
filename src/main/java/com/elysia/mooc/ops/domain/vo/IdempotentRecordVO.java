package com.elysia.mooc.ops.domain.vo;

import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 幂等记录前端展示对象。 */
@Data
public class IdempotentRecordVO {

    /** 幂等记录 ID。 */
    private Long id;

    /** 幂等键。 */
    private String idempotentKey;

    /** 业务类型。 */
    private String bizType;

    /** 业务 ID。 */
    private String bizId;

    /** 请求摘要。 */
    private String requestHash;

    /** 响应快照。 */
    private String responseBody;

    /** 处理状态。 */
    private IdempotentStatus status;

    /** 处理状态中文说明。 */
    private String statusText;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    private Long createBy;

    /** 更新人 ID。 */
    private Long updateBy;
}
