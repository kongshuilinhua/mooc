package com.elysia.mooc.common.enums;

import java.util.Objects;

/**
 * 业务枚举基础契约，统一数据库值、接口值和中文说明。
 *
 * @param <V> 枚举落库和接口输出值类型
 */
public interface BaseEnum<V> {

    /** 枚举落库和接口输出值。 */
    V getValue();

    /** 枚举中文说明。 */
    String getDesc();

    /**
     * 判断外部输入是否等于当前枚举值。
     *
     * @param value 外部输入值，支持数字、字符串和枚举名
     * @return 是否匹配当前枚举
     */
    default boolean equalsValue(Object value) {
        return matches(this, value);
    }

    /**
     * 按枚举值或枚举名解析业务枚举。
     *
     * @param enumType 枚举类型
     * @param value    外部输入值
     * @param <E>      枚举类型
     * @return 解析后的枚举；空白输入返回 null
     */
    static <E extends Enum<E> & BaseEnum<?>> E parse(Class<E> enumType, Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        for (E item : enumType.getEnumConstants()) {
            if (item.name().equalsIgnoreCase(text) || matches(item, value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("枚举值不合法：" + text);
    }

    /**
     * 判断输入值是否属于指定业务枚举。
     *
     * @param enumType 枚举类型
     * @param value    外部输入值
     * @return 是否合法
     */
    static boolean isValid(Class<? extends Enum<?>> enumType, Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return true;
        }
        if (!BaseEnum.class.isAssignableFrom(enumType)) {
            return false;
        }
        for (Enum<?> item : enumType.getEnumConstants()) {
            if (item.name().equalsIgnoreCase(String.valueOf(value).trim())
                    || matches((BaseEnum<?>) item, value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(BaseEnum<?> item, Object value) {
        Object enumValue = item.getValue();
        if (enumValue == null || value == null) {
            return Objects.equals(enumValue, value);
        }
        if (enumValue instanceof Number enumNumber && value instanceof Number inputNumber) {
            return Double.compare(enumNumber.doubleValue(), inputNumber.doubleValue()) == 0;
        }
        return String.valueOf(enumValue).equalsIgnoreCase(String.valueOf(value).trim());
    }
}
