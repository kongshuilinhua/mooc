package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 消息已读状态，数据库值：0 未读，1 已读。 */
@Getter
@RequiredArgsConstructor
public enum ReadStatus implements BaseEnum<Integer> {

    /** 未读。 */
    UNREAD(0, "未读"),

    /** 已读。 */
    READ(1, "已读");

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
    public static ReadStatus of(Object value) {
        return BaseEnum.parse(ReadStatus.class, value);
    }
}
