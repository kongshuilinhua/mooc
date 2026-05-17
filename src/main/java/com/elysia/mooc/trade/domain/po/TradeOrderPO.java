package com.elysia.mooc.trade.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 订单实体，映射 trade_order 表。 */
@Data
@TableName("trade_order")
public class TradeOrderPO {

    /** 订单 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号。 */
    private String orderNo;

    /** 用户 ID。 */
    private Long userId;

    /** 订单总金额。 */
    private BigDecimal totalAmount;

    /** 实付金额。 */
    private BigDecimal payAmount;

    /** 订单状态。 */
    private OrderStatus status;

    /** 过期时间。 */
    private LocalDateTime expireTime;

    /** 支付时间。 */
    private LocalDateTime payTime;

    /** 取消时间。 */
    private LocalDateTime cancelTime;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 ID。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人 ID。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
