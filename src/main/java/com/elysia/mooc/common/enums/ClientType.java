package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 客户端类型。 */
@Getter
@RequiredArgsConstructor
public enum ClientType implements BaseEnum<String> {

    /** Web 端。 */
    WEB("web", "Web端"),

    /** 移动端。 */
    MOBILE("mobile", "移动端"),

    /** 管理端。 */
    ADMIN("admin", "管理端");

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
    public static ClientType of(Object value) {
        return BaseEnum.parse(ClientType.class, value);
    }

    /**
     * 解析客户端类型，空值时使用默认值。
     *
     * @param value        外部输入
     * @param defaultValue 默认客户端类型
     * @return 客户端类型
     */
    public static ClientType ofOrDefault(Object value, ClientType defaultValue) {
        ClientType clientType = of(value);
        return clientType == null ? defaultValue : clientType;
    }
}
