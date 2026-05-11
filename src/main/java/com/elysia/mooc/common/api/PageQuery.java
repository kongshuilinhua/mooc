package com.elysia.mooc.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页查询参数。
 */
@Data
public class PageQuery {

    /** 页码，从 1 开始。 */
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNo = 1;

    /** 每页数量，默认 10，最大 100。 */
    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能大于 100")
    private Integer pageSize = 10;
}
