package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 互动逻辑删除状态。 */
@Getter
@RequiredArgsConstructor
public enum InteractionDeletedStatus implements BaseEnum<Integer> {

    /** 未删除。 */
    NORMAL(0, "未删除"),

    /** 已删除。 */
    DELETED(1, "已删除");

    /** 落库和接口输出值。 */
    @EnumValue
    private final Integer value;

    /** 中文说明。 */
    private final String desc;

    @JsonValue
    @Override
    public Integer getValue() {
        return value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InteractionDeletedStatus of(Object value) {
        return BaseEnum.parse(InteractionDeletedStatus.class, value);
    }
}
