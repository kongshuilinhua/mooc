package com.elysia.mooc.homework.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.homework.constants.HomeworkConstants;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 学生提交作业请求。 */
@Data
public class SubmitHomeworkRequest implements Checker {

    /** 作业 ID。 */
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;

    /** 提交正文，兼容 content 字段。 */
    @JsonAlias("content")
    @Size(max = 20000, message = "提交内容不能超过20000个字符")
    private String submitContent;

    /** 附件地址列表，当前序列化到 submit_content 中兼容保存。 */
    private List<String> attachmentUrls;

    @Override
    public void check() {
        if (assignmentId == null || assignmentId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "作业ID必须为正数");
        }
        boolean hasContent = StringUtils.hasText(submitContent);
        boolean hasAttachments = !CollectionUtils.isEmpty(attachmentUrls);
        if (!hasContent && !hasAttachments) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "提交内容和附件不能同时为空");
        }
        if (hasContent) {
            submitContent = submitContent.trim();
        }
        if (attachmentUrls != null && attachmentUrls.size() > HomeworkConstants.MAX_ATTACHMENT_SIZE) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "作业附件不能超过10个");
        }
        if (attachmentUrls != null) {
            attachmentUrls = attachmentUrls.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .toList();
        }
    }
}
