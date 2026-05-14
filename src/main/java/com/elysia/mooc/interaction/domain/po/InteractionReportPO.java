package com.elysia.mooc.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.interaction.domain.enums.InteractionTargetType;
import com.elysia.mooc.interaction.domain.enums.ReportStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 互动举报实体，映射 interaction_report 表。 */
@Data
@TableName("interaction_report")
public class InteractionReportPO {

    /** 举报 ID。 */
    @TableId(type = IdType.AUTO)
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
