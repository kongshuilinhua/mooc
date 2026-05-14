package com.elysia.mooc.interaction.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.elysia.mooc.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 回答采纳状态。 */
@Getter
@RequiredArgsConstructor
public enum AnswerAcceptedStatus implements BaseEnum<Integer> {

    /** 未采纳。 */
    NOT_ACCEPTED(0, "未采纳"),

    /** 已采纳。 */
    ACCEPTED(1, "已采纳");

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
    public static AnswerAcceptedStatus of(Object value) {
        return BaseEnum.parse(AnswerAcceptedStatus.class, value);
    }
}
