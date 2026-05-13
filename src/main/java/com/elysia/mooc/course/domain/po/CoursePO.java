package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.course.domain.enums.CourseDifficulty;
import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程实体，映射 course 表。 */
@Data
@TableName("course")
public class CoursePO {

    /** 课程 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程标题。 */
    private String title;

    /** 课程简介。 */
    private String summary;

    /** 课程详情。 */
    private String description;

    /** 课程封面地址。 */
    private String coverUrl;

    /** 分类 ID。 */
    private Long categoryId;

    /** 讲师用户 ID。 */
    private Long teacherId;

    /** 课程难度。 */
    private CourseDifficulty difficulty;

    /** 课程价格。 */
    private BigDecimal price;

    /** 课程状态。 */
    private CourseStatus status;

    /** 学习人数。 */
    private Integer learnCount;

    /** 收藏人数。 */
    private Integer favoriteCount;

    /** 平均评分。 */
    private BigDecimal ratingScore;

    /** 发布时间。 */
    private LocalDateTime publishTime;

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
