package com.elysia.mooc.trade.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/** 订单明细实体，映射 trade_order_item 表。 */
@Data
@TableName("trade_order_item")
public class TradeOrderItemPO {

    /** 订单明细 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单 ID。 */
    private Long orderId;

    /** 课程 ID。 */
    private Long courseId;

    /** 课程标题快照。 */
    private String courseTitle;

    /** 课程封面快照。 */
    private String courseCover;

    /** 单价。 */
    private BigDecimal price;

    /** 数量。 */
    private Integer quantity;
}
