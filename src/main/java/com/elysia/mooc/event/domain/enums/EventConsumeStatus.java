package com.elysia.mooc.event.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 事件消费状态。 */
@Getter
@RequiredArgsConstructor
public enum EventConsumeStatus implements BaseEnum<String> {

    /** 消费成功。 */
    SUCCESS("SUCCESS", "成功"),

    /** 消费失败。 */
    FAILED("FAILED", "失败");

    /** 落库和接口输出值。 */
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
    public static EventConsumeStatus of(Object value) {
        return BaseEnum.parse(EventConsumeStatus.class, value);
    }
}
