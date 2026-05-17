package com.elysia.mooc.trade.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 订单状态。 */
@Getter
@RequiredArgsConstructor
public enum OrderStatus implements BaseEnum<String> {

    /** 待支付。 */
    UNPAID("UNPAID", "待支付"),

    /** 已支付。 */
    PAID("PAID", "已支付"),

    /** 已取消。 */
    CANCELLED("CANCELLED", "已取消"),

    /** 已关闭。 */
    CLOSED("CLOSED", "已关闭");

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
    public static OrderStatus of(Object value) {
        return BaseEnum.parse(OrderStatus.class, value);
    }
}
