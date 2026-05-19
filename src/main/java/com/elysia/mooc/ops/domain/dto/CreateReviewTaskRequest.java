package com.elysia.mooc.ops.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.ops.domain.enums.OpsReviewPriority;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 创建审核任务请求。 */
@Data
public class CreateReviewTaskRequest implements Checker {

    /** 前端目标类型，落库时映射为 bizType。 */
    @Size(max = 32, message = "目标类型不能超过32个字符")
    private String targetType;

    /** 前端目标 ID，落库时映射为 bizId。 */
    private String targetId;

    /** 兼容后端语义的业务类型。 */
    @Size(max = 32, message = "业务类型不能超过32个字符")
    private String bizType;

    /** 兼容后端语义的业务 ID。 */
    private Long bizId;

    /** 提交用户 ID，不传时默认取当前管理员。 */
    private Long submitUserId;

    /** 审核说明。 */
    @Size(max = 2000, message = "审核说明不能超过2000个字符")
    private String reason;

    /** 兼容 SQL 字段语义的审核说明。 */
    @Size(max = 2000, message = "审核说明不能超过2000个字符")
    private String reviewReason;

    /** 审核优先级，仅作为接口与前端展示字段，当前 SQL 不单独落列。 */
    private OpsReviewPriority priority = OpsReviewPriority.MEDIUM;

    @Override
    public void check() {
        if (!StringUtils.hasText(bizType)) {
            bizType = normalizeType(targetType);
        }
        if (!StringUtils.hasText(targetType)) {
            targetType = normalizeType(bizType);
        }
        if (!StringUtils.hasText(bizType)) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "目标类型不能为空");
        }
        if (bizId == null) {
            bizId = parseTargetId(targetId);
        }
        if (!StringUtils.hasText(targetId) && bizId != null) {
            targetId = String.valueOf(bizId);
        }
        if (bizId == null || bizId <= 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "目标ID必须为正数");
        }
        if (!StringUtils.hasText(reviewReason)) {
            reviewReason = trimText(reason);
        }
        if (!StringUtils.hasText(reason)) {
            reason = trimText(reviewReason);
        }
        if (priority == null) {
            priority = OpsReviewPriority.MEDIUM;
        }
    }

    private Long parseTargetId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "目标ID必须为正数");
        }
    }

    private String normalizeType(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private String trimText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
