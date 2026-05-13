package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 课程标签视图对象。 */
@Data
public class CourseTagVO {

    /** 标签 ID。 */
    private Long id;

    /** 标签名称。 */
    private String name;

    /** 使用次数。 */
    private Integer useCount;

    /** 启停状态。 */
    private EnableStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
