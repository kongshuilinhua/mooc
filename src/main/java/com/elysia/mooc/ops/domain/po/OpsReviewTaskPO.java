package com.elysia.mooc.ops.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ops.domain.enums.OpsReviewStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 审核任务实体，映射 ops_review_task 表。 */
@Data
@TableName("ops_review_task")
public class OpsReviewTaskPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型。 */
    private String bizType;

    /** 业务 ID。 */
    private Long bizId;

    /** 提交用户 ID。 */
    private Long submitUserId;

    /** 审核状态。 */
    private OpsReviewStatus reviewStatus;

    /** 审核人 ID。 */
    private Long reviewerId;

    /** 审核时间。 */
    private LocalDateTime reviewTime;

    /** 审核说明。 */
    private String reviewReason;

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
}
