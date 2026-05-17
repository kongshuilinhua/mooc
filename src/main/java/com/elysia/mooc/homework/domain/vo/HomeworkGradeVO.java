package com.elysia.mooc.homework.domain.vo;

import com.elysia.mooc.homework.domain.enums.HomeworkGradeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 作业批改响应。 */
@Data
public class HomeworkGradeVO {

    /** 批改记录 ID。 */
    private Long id;

    /** 提交记录 ID。 */
    private Long submissionId;

    /** 批改教师 ID。 */
    private Long teacherId;

    /** 分数。 */
    private BigDecimal score;

    /** 批改评语。 */
    private String feedback;

    /** 批改状态。 */
    private HomeworkGradeStatus gradeStatus;

    /** 批改状态中文说明。 */
    private String gradeStatusDesc;

    /** 批改时间。 */
    private LocalDateTime gradeTime;

    /** 通知消息 ID。 */
    private Long messageId;
}
