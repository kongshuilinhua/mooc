package com.elysia.mooc.trade.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 支付记录状态。 */
@Getter
@RequiredArgsConstructor
public enum PayStatus implements BaseEnum<String> {

    /** 待支付。 */
    PENDING("PENDING", "待支付"),

    /** 支付成功。 */
    SUCCESS("SUCCESS", "支付成功"),

    /** 支付失败。 */
    FAILED("FAILED", "支付失败"),

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
    public static PayStatus of(Object value) {
        return BaseEnum.parse(PayStatus.class, value);
    }
}
