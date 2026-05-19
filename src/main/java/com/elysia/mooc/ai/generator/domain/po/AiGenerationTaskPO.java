package com.elysia.mooc.ai.generator.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ai.generator.domain.enums.AiGenerationBizType;
import com.elysia.mooc.ai.generator.domain.enums.AiGenerationStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** AI 生成任务实体，映射 ai_generation_task 表。 */
@Data
@TableName("ai_generation_task")
public class AiGenerationTaskPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务类型。 */
    private AiGenerationBizType bizType;

    /** 业务 ID。 */
    private Long bizId;

    /** 触发用户 ID。 */
    private Long triggerUserId;

    /** 任务状态。 */
    private AiGenerationStatus status;

    /** Prompt 快照，SQL 字段为 LONGTEXT。 */
    private String promptSnapshot;

    /** 结果快照，SQL 字段为 LONGTEXT。 */
    private String resultSnapshot;

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
