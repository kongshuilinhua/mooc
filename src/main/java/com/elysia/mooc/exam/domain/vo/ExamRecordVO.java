package com.elysia.mooc.exam.domain.vo;

import com.elysia.mooc.exam.domain.enums.ExamRecordStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/** 作答记录响应。 */
@Data
public class ExamRecordVO {

    /** 作答记录 ID。 */
    private Long id;

    /** 试卷 ID。 */
    private Long paperId;

    /** 作答用户 ID。 */
    private Long userId;

    /** 总得分，简答题待批改时只包含客观题得分。 */
    private BigDecimal score;

    /** 是否通过，待人工批改时为 null。 */
    private Boolean passed;

    /** 作答状态。 */
    private ExamRecordStatus status;

    /** 是否存在待人工批改题目。 */
    private Boolean manualReviewRequired;

    /** 提交时间。 */
    private LocalDateTime submitTime;

    /** 单题作答结果。 */
    private List<ExamAnswerRecordVO> answers;
}
