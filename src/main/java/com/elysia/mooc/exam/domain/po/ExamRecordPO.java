package com.elysia.mooc.exam.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.exam.domain.enums.ExamRecordStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 作答记录实体，映射 exam_record 表。 */
@Data
@TableName("exam_record")
public class ExamRecordPO {

    /** 作答记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷 ID。 */
    private Long paperId;

    /** 作答用户 ID。 */
    private Long userId;

    /** 开始时间。 */
    private LocalDateTime startTime;

    /** 提交时间。 */
    private LocalDateTime submitTime;

    /** 当前得分，主观题待批改时只包含客观题得分。 */
    private BigDecimal score;

    /** 是否通过：1 通过，0 未通过，null 表示待人工批改。 */
    private Integer passed;

    /** 作答状态。 */
    private ExamRecordStatus status;

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
