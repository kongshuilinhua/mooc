package com.elysia.mooc.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 树形数据组装工具。 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * 按 id/parentId 组装树结构，根节点自动识别为 parentId 为空或父节点不存在的数据。
     *
     * @param data           原始列表
     * @param idGetter       ID 获取函数
     * @param parentIdGetter 父 ID 获取函数
     * @param childrenSetter 子节点写入函数
     * @param <T>            节点类型
     * @param <K>            ID 类型
     * @return 树形列表
     */
    public static <T, K> List<T> buildTree(
            List<T> data,
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter) {
        return buildTree(data, null, idGetter, parentIdGetter, childrenSetter);
    }

    /**
     * 按指定根 parentId 组装树结构。
     *
     * @param data           原始列表
     * @param rootParentId   根节点父 ID
     * @param idGetter       ID 获取函数
     * @param parentIdGetter 父 ID 获取函数
     * @param childrenSetter 子节点写入函数
     * @param <T>            节点类型
     * @param <K>            ID 类型
     * @return 树形列表
     */
    public static <T, K> List<T> buildTree(
            List<T> data,
            K rootParentId,
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        Map<K, List<T>> childrenMap = new HashMap<>();
        data.forEach(item -> childrenMap
                .computeIfAbsent(parentIdGetter.apply(item), key -> new java.util.ArrayList<>())
                .add(item));
        Set<K> idSet = data.stream().map(idGetter).collect(Collectors.toSet());
        data.forEach(item -> childrenSetter.accept(
                item,
                childrenMap.getOrDefault(idGetter.apply(item), Collections.emptyList())));
        return data.stream()
                .filter(item -> {
                    K parentId = parentIdGetter.apply(item);
                    return Objects.equals(parentId, rootParentId)
                            || parentId == null
                            || !idSet.contains(parentId);
                })
                .collect(Collectors.toList());
    }
}
