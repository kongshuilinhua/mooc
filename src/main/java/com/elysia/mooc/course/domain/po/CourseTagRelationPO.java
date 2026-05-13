package com.elysia.mooc.course.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程标签关系实体，映射 course_tag_relation 表。 */
@Data
@TableName("course_tag_relation")
public class CourseTagRelationPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 标签 ID。 */
    private Long tagId;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
