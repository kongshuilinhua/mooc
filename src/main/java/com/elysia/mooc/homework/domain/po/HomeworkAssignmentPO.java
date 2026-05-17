package com.elysia.mooc.homework.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业主表实体，映射 homework_assignment。 */
@Data
@TableName("homework_assignment")
public class HomeworkAssignmentPO {

    /** 作业 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节 ID，可为空。 */
    private Long chapterId;

    /** 作业标题。 */
    private String title;

    /** 作业说明。 */
    private String description;

    /** 截止时间。 */
    private LocalDateTime deadlineTime;

    /** 作业状态。 */
    private HomeworkAssignmentStatus status;

    /** 是否允许重复提交，0 否，1 是。 */
    private Integer allowResubmit;

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
