package com.elysia.mooc.trade.domain.dto;

import com.elysia.mooc.common.api.PageQuery;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 我的订单分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQuery extends PageQuery {

    /** 搜索关键字，匹配订单号或课程标题。 */
    @Size(max = 50, message = "搜索关键字不能超过50个字符")
    private String keyword;

    /** 订单状态。 */
    private OrderStatus status;

    /** 排序字段白名单：createTime、payTime、payAmount。 */
    private String sortBy;

    /** 是否升序。 */
    private Boolean isAsc = false;
}
