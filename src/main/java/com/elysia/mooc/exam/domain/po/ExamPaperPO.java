package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.exam.domain.enums.ExamPaperStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 试卷实体，映射 exam_paper 表。 */
@Data
@TableName("exam_paper")
public class ExamPaperPO {

    /** 试卷 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 试卷标题。 */
    private String title;

    /** 试卷描述。 */
    private String description;

    /** 总分。 */
    private BigDecimal totalScore;

    /** 及格分。 */
    private BigDecimal passScore;

    /** 考试时长，单位分钟。 */
    private Integer durationMinutes;

    /** 试卷状态。 */
    private ExamPaperStatus status;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
