package com.elysia.mooc.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询结果。
 * 所有分页接口统一使用此结构返回。
 *
 * @param <T> 分页记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Integer totalPage;

    /** 当前页记录列表 */
    private List<T> list;

    /**
     * 构造空分页结果。
     *
     * @param total     总记录数
     * @param totalPage 总页数
     * @param <T>       记录类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Long total, Integer totalPage) {
        return new PageResult<>(total, totalPage, Collections.emptyList());
    }

    /**
     * 根据总数、页大小和列表构造分页结果。
     *
     * @param total    总记录数
     * @param pageSize 每页数量
     * @param list     当前页列表
     * @param <T>      记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, Integer pageSize, List<T> list) {
        long safeTotal = total == null ? 0L : total;
        int safePageSize = pageSize == null || pageSize <= 0 ? 1 : pageSize;
        int totalPage = safeTotal == 0 ? 0 : (int) Math.ceil((double) safeTotal / safePageSize);
        return new PageResult<>(safeTotal, totalPage, list == null ? Collections.emptyList() : list);
    }

    /**
     * 根据 MyBatis Plus 分页对象构造空结果。
     *
     * @param page 分页对象
     * @param <T>  记录类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(Page<?> page) {
        if (page == null) {
            return new PageResult<>(0L, 0, Collections.emptyList());
        }
        return new PageResult<>(page.getTotal(), toIntPage(page.getPages()), Collections.emptyList());
    }

    /**
     * 根据 MyBatis Plus 分页对象构造结果。
     *
     * @param page 分页对象
     * @param <T>  记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        if (page == null) {
            return empty(0L, 0);
        }
        List<T> records = page.getRecords() == null ? Collections.emptyList() : page.getRecords();
        return new PageResult<>(page.getTotal(), toIntPage(page.getPages()), records);
    }

    /**
     * 根据 MyBatis Plus 分页对象和转换函数构造结果。
     *
     * @param page   分页对象
     * @param mapper 记录转换函数
     * @param <R>    原记录类型
     * @param <T>    目标记录类型
     * @return 分页结果
     */
    public static <R, T> PageResult<T> of(Page<R> page, Function<R, T> mapper) {
        if (page == null) {
            return empty(0L, 0);
        }
        List<T> records = page.getRecords() == null
                ? Collections.emptyList()
                : page.getRecords().stream().map(mapper).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), toIntPage(page.getPages()), records);
    }

    /**
     * 根据分页对象和外部组装后的列表构造结果。
     *
     * @param page 分页对象
     * @param list 已转换的记录列表
     * @param <T>  目标记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<?> page, List<T> list) {
        if (page == null) {
            return new PageResult<>(0L, 0, list == null ? Collections.emptyList() : list);
        }
        return new PageResult<>(
                page.getTotal(),
                toIntPage(page.getPages()),
                list == null ? Collections.emptyList() : list);
    }

    private static int toIntPage(long pages) {
        return pages > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) pages;
    }
}
