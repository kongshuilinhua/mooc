package com.elysia.mooc.learning.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.learning.domain.enums.LearningFinishedStatus;
import com.elysia.mooc.learning.domain.enums.LearningSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 我的课程实体，映射 learning_course 表。 */
@Data
@TableName("learning_course")
public class LearningCoursePO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 课程 ID。 */
    private Long courseId;

    /** 加入来源。 */
    private LearningSource source;

    /** 课程学习进度百分比。 */
    private BigDecimal progressPercent;

    /** 累计学习秒数。 */
    private Integer learnedSeconds;

    /** 最近学习小节 ID。 */
    private Long lastSectionId;

    /** 最近学习时间。 */
    private LocalDateTime lastLearnTime;

    /** 是否完成。 */
    private LearningFinishedStatus finished;

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
