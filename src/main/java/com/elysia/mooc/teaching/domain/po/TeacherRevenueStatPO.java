package com.elysia.mooc.teaching.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 教师收入日统计实体，映射 teacher_revenue_stat 表。 */
@Data
@TableName("teacher_revenue_stat")
public class TeacherRevenueStatPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 教师用户 ID。 */
    private Long teacherId;

    /** 课程 ID。 */
    private Long courseId;

    /** 统计日期。 */
    private LocalDate statDate;

    /** 支付订单数。 */
    private Integer paidOrderCount;

    /** 收入金额。 */
    private BigDecimal incomeAmount;

    /** 退款金额。 */
    private BigDecimal refundAmount;

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
