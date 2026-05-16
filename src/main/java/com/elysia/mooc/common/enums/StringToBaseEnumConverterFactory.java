package com.elysia.mooc.common.enums;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        T parsed = invokeCustomParser(targetType, source);
        if (parsed != null) {
            return parsed;
        }
        return (T) BaseEnum.parse((Class) targetType, source);
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseEnum<?>> T invokeCustomParser(Class<T> targetType, String source) {
        try {
            Method method = targetType.getMethod("of", Object.class);
            return (T) method.invoke(null, source);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("业务枚举解析方法不可访问", e);
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalArgumentException("业务枚举解析失败", target);
        }
    }
}
