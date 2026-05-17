package com.elysia.mooc.trade.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.trade.domain.enums.PayChannel;
import com.elysia.mooc.trade.domain.enums.PayStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 支付记录实体，映射 pay_record 表。 */
@Data
@TableName("pay_record")
public class PayRecordPO {

    /** 支付记录 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号。 */
    private String payNo;

    /** 订单 ID。 */
    private Long orderId;

    /** 订单号。 */
    private String orderNo;

    /** 支付渠道。 */
    private PayChannel payChannel;

    /** 支付金额。 */
    private BigDecimal amount;

    /** 支付状态。 */
    private PayStatus status;

    /** 模拟回调报文。 */
    private String callbackPayload;

    /** 支付时间。 */
    private LocalDateTime payTime;

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
