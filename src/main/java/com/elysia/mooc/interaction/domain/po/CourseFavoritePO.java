package com.elysia.mooc.interaction.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程收藏实体，映射 course_favorite 表。 */
@Data
@TableName("course_favorite")
public class CourseFavoritePO {

    /** 收藏 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 用户 ID。 */
    private Long userId;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 逻辑删除标记。 */
    private Integer deleted;
}
