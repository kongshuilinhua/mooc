package com.elysia.mooc.common.idempotent.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.idempotent.domain.enums.IdempotentStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 幂等记录实体，映射 idempotent_record 表。 */
@Data
@TableName("idempotent_record")
public class IdempotentRecordPO {

    /** 幂等记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 已组合业务类型、用户和请求 key 的幂等键。 */
    private String idempotentKey;

    /** 业务类型。 */
    private String bizType;

    /** 业务 ID。 */
    private String bizId;

    /** 请求摘要，用于防止同一 key 被不同请求复用。 */
    private String requestHash;

    /** 成功响应快照。 */
    private String responseBody;

    /** 处理状态。 */
    private IdempotentStatus status;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;

    /** 是否本次请求新建，非数据库字段，仅供切面判断是否继续执行业务。 */
    @TableField(exist = false)
    private Boolean newlyCreated;
}
