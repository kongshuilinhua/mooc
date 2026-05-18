package com.elysia.mooc.studyarchive.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 学习报告实体，映射 learning_report 表。 */
@Data
@TableName("learning_report")
public class LearningReportPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 报告日期。 */
    private LocalDate reportDate;

    /** 学习分钟数。 */
    private Integer studyMinutes;

    /** 完成小节数。 */
    private Integer completedSections;

    /** 错题数。 */
    private Integer wrongCount;

    /** AI 提问次数。 */
    private Integer aiAskCount;

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
