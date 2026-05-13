package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程知识点实体，映射 course_concept 表。 */
@Data
@TableName("course_concept")
public class CourseConceptPO {

    /** 知识点 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 关联小节 ID，空值表示课程级知识点。 */
    private Long sectionId;

    /** 知识点标题。 */
    private String title;

    /** 知识点说明。 */
    private String content;

    /** 视频内开始秒数。 */
    private Integer startSecond;

    /** 视频内结束秒数。 */
    private Integer endSecond;

    /** 展示排序。 */
    private Integer sort;

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
