package com.elysia.mooc.course.domain.vo;

import com.elysia.mooc.course.domain.enums.CourseStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 课程变更结果视图对象。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMutationVO {

    /** 课程 ID。 */
    private Long id;

    /** 当前课程状态。 */
    private CourseStatus status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
