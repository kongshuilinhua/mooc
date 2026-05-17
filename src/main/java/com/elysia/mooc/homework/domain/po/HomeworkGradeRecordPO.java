package com.elysia.mooc.homework.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业批改记录实体，映射 homework_grade_record。 */
@Data
@TableName("homework_grade_record")
public class HomeworkGradeRecordPO {

    /** 批改记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提交记录 ID。 */
    private Long submissionId;

    /** 批改教师 ID。 */
    private Long teacherId;

    /** 评分。 */
    private BigDecimal score;

    /** 批改评语。 */
    private String feedback;

    /** 批改时间。 */
    private LocalDateTime gradeTime;

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
