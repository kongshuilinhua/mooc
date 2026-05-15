package com.elysia.mooc.event.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 事件发布状态。 */
@Getter
@RequiredArgsConstructor
public enum EventPublishStatus implements BaseEnum<String> {

    /** 待发送。 */
    PENDING("PENDING", "待发送"),

    /** 已发送。 */
    SENT("SENT", "已发送"),

    /** 发送失败。 */
    FAILED("FAILED", "发送失败");

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
    public static EventPublishStatus of(Object value) {
        return BaseEnum.parse(EventPublishStatus.class, value);
    }
}
