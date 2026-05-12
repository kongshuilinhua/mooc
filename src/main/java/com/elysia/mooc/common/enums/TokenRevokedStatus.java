package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 刷新令牌撤销状态，数据库值：0 有效，1 已撤销。 */
@Getter
@RequiredArgsConstructor
public enum TokenRevokedStatus implements BaseEnum<Integer> {

    /** 有效。 */
    VALID(0, "有效"),

    /** 已撤销。 */
    REVOKED(1, "已撤销");

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
    public static TokenRevokedStatus of(Object value) {
        return BaseEnum.parse(TokenRevokedStatus.class, value);
    }
}
