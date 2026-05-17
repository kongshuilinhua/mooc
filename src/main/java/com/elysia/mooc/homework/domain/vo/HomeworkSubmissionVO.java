package com.elysia.mooc.homework.domain.vo;

import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业提交响应。 */
@Data
public class HomeworkSubmissionVO {

    /** 提交记录 ID。 */
    private Long id;

    /** 兼容前端的提交记录 ID。 */
    private Long submissionId;

    /** 作业 ID。 */
    private Long assignmentId;

    /** 学生用户 ID。 */
    private Long studentId;

    /** 提交内容。 */
    private String submitContent;

    /** 提交时间。 */
    private LocalDateTime submitTime;

    /** 分数。 */
    private BigDecimal score;

    /** 批改状态。 */
    private HomeworkGradeStatus gradeStatus;

    /** 批改状态中文说明。 */
    private String gradeStatusDesc;
}
