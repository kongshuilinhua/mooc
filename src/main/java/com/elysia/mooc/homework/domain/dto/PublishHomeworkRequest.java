package com.elysia.mooc.homework.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.homework.domain.enums.HomeworkAssignmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 教师发布作业请求。 */
@Data
public class PublishHomeworkRequest implements Checker {

    /** 课程 ID。 */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 章节 ID，可为空。 */
    private Long chapterId;

    /** 作业标题。 */
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 128, message = "作业标题不能超过128个字符")
    private String title;

    /** 作业说明。 */
    private String description;

    /** 截止时间。 */
    private LocalDateTime deadlineTime;

    /** 是否允许重复提交。 */
    private Boolean allowResubmit = Boolean.FALSE;

    /** 作业状态，默认直接发布。 */
    private HomeworkAssignmentStatus status = HomeworkAssignmentStatus.PUBLISHED;

    @Override
    public void check() {
        if (courseId == null || courseId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "课程ID必须为正数");
        }
        if (chapterId != null && chapterId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "章节ID必须为正数");
        }
        if (!StringUtils.hasText(title)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "作业标题不能为空");
        }
        title = title.trim();
        if (deadlineTime != null && !deadlineTime.isAfter(LocalDateTime.now())) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "作业截止时间必须晚于当前时间");
        }
        if (status == null) {
            status = HomeworkAssignmentStatus.PUBLISHED;
        }
    }
}
