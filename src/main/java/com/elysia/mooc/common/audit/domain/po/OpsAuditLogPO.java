package com.elysia.mooc.common.audit.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 审计日志实体，映射 ops_audit_log 表。 */
@Data
@TableName("ops_audit_log")
public class OpsAuditLogPO {

    /** 审计日志 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 ID，匿名或系统任务可为空。 */
    private Long operatorId;

    /** 操作人名称。 */
    private String operatorName;

    /** 操作动作编码。 */
    private String action;

    /** 操作目标类型。 */
    private String targetType;

    /** 操作目标 ID。 */
    private String targetId;

    /** 请求方法。 */
    private String requestMethod;

    /** 请求路径。 */
    private String requestPath;

    /** 请求来源 IP。 */
    private String requestIp;

    /** 链路追踪 ID。 */
    private String traceId;

    /** 是否成功：1 成功，0 失败。 */
    private Integer success;

    /** 错误信息摘要。 */
    private String errorMessage;

    /** 执行耗时，单位毫秒。 */
    private Integer costMs;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
