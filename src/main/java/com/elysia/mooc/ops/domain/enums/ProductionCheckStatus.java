package com.elysia.mooc.ops.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 阶段一生产化巡检状态。 */
@Getter
@RequiredArgsConstructor
public enum ProductionCheckStatus implements BaseEnum<String> {

    /** 巡检通过。 */
    PASS("PASS", "通过"),

    /** 存在需要关注但不一定阻塞启动的问题。 */
    WARN("WARN", "警告"),

    /** 存在阻塞联调或演示的缺口。 */
    FAILED("FAILED", "失败");

    /** 接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ProductionCheckStatus of(Object value) {
        return BaseEnum.parse(ProductionCheckStatus.class, value);
    }
}
