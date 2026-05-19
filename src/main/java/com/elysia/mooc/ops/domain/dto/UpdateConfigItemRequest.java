package com.elysia.mooc.ops.domain.dto;

import com.elysia.mooc.common.error.CommonErrorCode;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.common.validate.Checker;
import com.elysia.mooc.ops.constants.OpsConfigConstants;
import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsConfigValueType;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/** 更新运营配置项请求。 */
@Data
public class UpdateConfigItemRequest implements Checker {

    /** 配置分组，不传时由配置键前缀推断。 */
    @Size(max = 64, message = "配置分组不能超过64个字符")
    private String configGroup;

    /** 前端传入的配置值。 */
    private String value;

    /** 兼容后端语义的配置值。 */
    private String configValue;

    /** 配置值类型。 */
    private OpsConfigValueType valueType;

    /** 配置启停状态。 */
    private OpsConfigStatus status;

    /** 变更说明，当前记录到响应和审计日志，SQL 不单独落列。 */
    @Size(max = 500, message = "配置备注不能超过500个字符")
    private String remark;

    @Override
    public void check() {
        if (!StringUtils.hasText(configValue)) {
            configValue = value;
        }
        if (!StringUtils.hasText(value)) {
            value = configValue;
        }
        if (configValue == null) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "配置值不能为空");
        }
        if (configValue.length() > OpsConfigConstants.CONFIG_VALUE_MAX_LENGTH) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "配置值长度不能超过20000个字符");
        }
        if (StringUtils.hasText(configGroup)) {
            configGroup = configGroup.trim().toUpperCase();
        }
    }
}
