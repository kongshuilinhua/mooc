package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 导出任务状态。 */
@Getter
@RequiredArgsConstructor
public enum OpsExportJobStatus implements BaseEnum<String> {

    /** 等待处理。 */
    PENDING("PENDING", "等待处理"),

    /** 处理中。 */
    PROCESSING("PROCESSING", "处理中"),

    /** 处理成功。 */
    SUCCESS("SUCCESS", "处理成功"),

    /** 处理失败。 */
    FAILED("FAILED", "处理失败");

    @EnumValue
    private final String value;

    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OpsExportJobStatus of(Object value) {
        return BaseEnum.parse(OpsExportJobStatus.class, value);
    }
}
