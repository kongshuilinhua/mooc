package com.elysia.mooc.trade.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 支付渠道。 */
@Getter
@RequiredArgsConstructor
public enum PayChannel implements BaseEnum<String> {

    /** 模拟支付。 */
    MOCK("MOCK", "模拟支付");

    /** 落库和接口输出值。 */
    @EnumValue
    private final String value;

    /** 中文说明。 */
    private final String desc;

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PayChannel of(Object value) {
        return BaseEnum.parse(PayChannel.class, value);
    }
}
