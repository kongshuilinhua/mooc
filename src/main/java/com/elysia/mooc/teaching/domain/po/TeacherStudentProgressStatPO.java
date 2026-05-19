package com.elysia.mooc.teaching.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.teaching.domain.enums.TeacherStudentRiskLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 教师学员进度统计实体，映射 teacher_student_progress_stat 表。 */
@Data
@TableName("teacher_student_progress_stat")
public class TeacherStudentProgressStatPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 教师用户 ID。 */
    private Long teacherId;

    /** 课程 ID。 */
    private Long courseId;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 学习进度百分比。 */
    private BigDecimal progressPercent;

    /** 最近学习时间。 */
    private LocalDateTime lastLearnTime;

    /** 学员风险等级。 */
    private TeacherStudentRiskLevel riskLevel;

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
