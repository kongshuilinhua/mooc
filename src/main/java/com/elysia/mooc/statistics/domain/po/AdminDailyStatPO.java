package com.elysia.mooc.statistics.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 后台每日统计实体，映射 admin_daily_stat 表。 */
@Data
@TableName("admin_daily_stat")
public class AdminDailyStatPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期。 */
    private LocalDate statDate;

    /** 新增用户数。 */
    private Integer newUserCount;

    /** 活跃用户数。 */
    private Integer activeUserCount;

    /** 课程浏览次数。 */
    private Integer courseViewCount;

    /** 视频播放次数。 */
    private Integer videoPlayCount;

    /** 学习时长，单位秒。 */
    private Long learnSeconds;

    /** AI 调用次数。 */
    private Integer aiCallCount;

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
