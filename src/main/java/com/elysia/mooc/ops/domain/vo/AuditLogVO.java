package com.elysia.mooc.ops.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 审计日志前端展示对象。 */
@Data
public class AuditLogVO {

    /** 审计日志 ID。 */
    private Long id;

    /** 操作人 ID。 */
    private Long operatorId;

    /** 操作人名称。 */
    private String operatorName;

    /** 操作动作编码。 */
    private String action;

    /** 目标类型。 */
    private String targetType;

    /** 目标 ID。 */
    private String targetId;

    /** 请求方法。 */
    private String requestMethod;

    /** 请求路径。 */
    private String requestPath;

    /** 请求来源 IP。 */
    private String requestIp;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 是否成功。 */
    private Boolean success;

    /** 错误摘要。 */
    private String errorMessage;

    /** 耗时，单位毫秒。 */
    private Integer costMs;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
