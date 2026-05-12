package com.elysia.mooc.common.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

/** Bean 转换工具，仅用于字段语义一致的对象复制。 */
public final class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    /**
     * 复制单个 Bean。
     *
     * @param source 原对象
     * @param clazz  目标类型
     * @param <S>    原对象类型
     * @param <T>    目标对象类型
     * @return 目标对象
     */
    public static <S, T> T copyBean(S source, Class<T> clazz) {
        return copyBean(source, clazz, null);
    }

    /**
     * 复制单个 Bean，并允许补充自定义转换。
     *
     * @param source    原对象
     * @param clazz     目标类型
     * @param converter 自定义转换回调
     * @param <S>       原对象类型
     * @param <T>       目标对象类型
     * @return 目标对象
     */
    public static <S, T> T copyBean(S source, Class<T> clazz, BiConsumer<S, T> converter) {
        if (source == null) {
            return null;
        }
        T target = BeanUtils.instantiateClass(clazz);
        BeanUtils.copyProperties(source, target);
        if (converter != null) {
            converter.accept(source, target);
        }
        return target;
    }

    /**
     * 复制 Bean 列表。
     *
     * @param sourceList 原对象列表
     * @param clazz      目标类型
     * @param <S>        原对象类型
     * @param <T>        目标对象类型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> clazz) {
        return copyList(sourceList, clazz, null);
    }

    /**
     * 复制 Bean 列表，并允许补充自定义转换。
     *
     * @param sourceList 原对象列表
     * @param clazz      目标类型
     * @param converter  自定义转换回调
     * @param <S>        原对象类型
     * @param <T>        目标对象类型
     * @return 目标对象列表
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> clazz, BiConsumer<S, T> converter) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> copyBean(source, clazz, converter))
                .collect(Collectors.toList());
    }
}
