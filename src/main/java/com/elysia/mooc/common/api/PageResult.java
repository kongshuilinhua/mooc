package com.elysia.mooc.common.api;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页查询结果。
 *
 * @param <T> 分页记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 总记录数。 */
    private Long total;

    /** 当前页码。 */
    private Integer pageNo;

    /** 每页数量。 */
    private Integer pageSize;

    /** 当前页记录列表。 */
    private List<T> records;
}
