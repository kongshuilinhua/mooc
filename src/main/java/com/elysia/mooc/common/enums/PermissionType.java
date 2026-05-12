package com.elysia.mooc.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 权限类型。 */
@Getter
@RequiredArgsConstructor
public enum PermissionType implements BaseEnum<String> {

    /** 菜单权限。 */
    MENU("MENU", "菜单"),

    /** 按钮权限。 */
    BUTTON("BUTTON", "按钮"),

    /** API 接口权限。 */
    API("API", "接口");

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
    public static PermissionType of(Object value) {
        return BaseEnum.parse(PermissionType.class, value);
    }
}
