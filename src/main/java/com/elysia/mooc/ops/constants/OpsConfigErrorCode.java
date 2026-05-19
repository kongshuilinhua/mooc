package com.elysia.mooc.ops.constants;

import com.elysia.mooc.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 运营配置模块错误码。 */
@Getter
@RequiredArgsConstructor
public enum OpsConfigErrorCode implements ErrorCode {

    /** 运营配置参数不合法。 */
    OPS_CONFIG_PARAM_INVALID(29001, "运营配置参数不正确"),

    /** 同一业务对象已存在待处理审核任务。 */
    OPS_REVIEW_PENDING_DUPLICATED(29002, "该业务对象已存在待处理审核任务"),

    /** 导出类型不在白名单内。 */
    OPS_EXPORT_TYPE_INVALID(29003, "导出类型不支持"),

    /** 配置键不合法。 */
    OPS_CONFIG_KEY_INVALID(29004, "配置键不合法"),

    /** 配置值与值类型不匹配。 */
    OPS_CONFIG_VALUE_INVALID(29005, "配置值与值类型不匹配");

    private final int code;
    private final String message;

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return this == OPS_REVIEW_PENDING_DUPLICATED ? 409 : 400;
    }
}
