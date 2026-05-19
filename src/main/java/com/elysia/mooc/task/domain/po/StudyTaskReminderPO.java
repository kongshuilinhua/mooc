package com.elysia.mooc.task.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderChannel;
import com.elysia.mooc.task.domain.enums.StudyTaskReminderStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习任务提醒实体，映射 study_task_reminder 表。 */
@Data
@TableName("study_task_reminder")
public class StudyTaskReminderPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务实例 ID。 */
    private Long instanceId;

    /** 提醒渠道。 */
    private StudyTaskReminderChannel remindChannel;

    /** 提醒时间。 */
    private LocalDateTime remindTime;

    /** 发送状态。 */
    private StudyTaskReminderStatus sendStatus;

    /** 发送结果。 */
    private String sendResult;

    /** 重试次数。 */
    private Integer retryCount;

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
