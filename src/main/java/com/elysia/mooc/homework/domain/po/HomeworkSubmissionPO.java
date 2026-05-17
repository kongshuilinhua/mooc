package com.elysia.mooc.homework.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业提交实体，映射 homework_submission。 */
@Data
@TableName("homework_submission")
public class HomeworkSubmissionPO {

    /** 提交记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作业 ID。 */
    private Long assignmentId;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 提交内容，可保存正文或 JSON 结构。 */
    private String submitContent;

    /** 提交时间。 */
    private LocalDateTime submitTime;

    /** 得分。 */
    private BigDecimal score;

    /** 批改状态。 */
    private HomeworkGradeStatus gradeStatus;

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
