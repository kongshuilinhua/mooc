package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程小节实体，映射 course_section 表。 */
@Data
@TableName("course_section")
public class CourseSectionPO {

    /** 小节 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节 ID。 */
    private Long chapterId;

    /** 小节标题。 */
    private String title;

    /** 媒资 ID。 */
    private Long mediaId;

    /** 视频时长，单位秒。 */
    private Integer durationSeconds;

    /** 是否可试看。 */
    private Boolean freePreview;

    /** 展示排序。 */
    private Integer sort;

    /** 小节启停状态。 */
    private EnableStatus status;

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
