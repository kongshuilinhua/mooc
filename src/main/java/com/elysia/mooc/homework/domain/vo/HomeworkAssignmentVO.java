package com.elysia.mooc.homework.domain.vo;

import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业响应。 */
@Data
public class HomeworkAssignmentVO {

    /** 作业 ID。 */
    private Long id;

    /** 课程 ID。 */
    private Long courseId;

    /** 章节 ID。 */
    private Long chapterId;

    /** 作业标题。 */
    private String title;

    /** 作业说明。 */
    private String description;

    /** 截止时间。 */
    private LocalDateTime deadlineTime;

    /** 状态。 */
    private HomeworkAssignmentStatus status;

    /** 状态中文说明。 */
    private String statusDesc;

    /** 是否允许重复提交。 */
    private Boolean allowResubmit;

    /** 发布时间。 */
    private LocalDateTime publishTime;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
