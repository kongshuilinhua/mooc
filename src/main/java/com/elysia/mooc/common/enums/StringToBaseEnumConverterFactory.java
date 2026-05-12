package com.elysia.mooc.common.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/** Spring MVC 查询参数到业务枚举的转换工厂。 */
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum<?>> {

    @Override
    public <T extends BaseEnum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return source -> convert(targetType, source);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends BaseEnum<?>> T convert(Class<T> targetType, String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        if (!targetType.isEnum()) {
            throw new IllegalArgumentException("目标类型不是业务枚举");
        }
        return (T) BaseEnum.parse((Class) targetType, source);
    }
}
