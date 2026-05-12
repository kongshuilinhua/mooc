package com.elysia.mooc.rbac.domain.dto;

import com.elysia.mooc.common.enums.EnableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 更新用户状态请求参数 */
@Data
public class UpdateUserStatusRequest {

    /** 目标状态：1/ENABLED 启用，0/DISABLED 禁用 */
    @NotNull(message = "状态不能为空")
    private EnableStatus status;
}
