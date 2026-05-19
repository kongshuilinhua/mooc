package com.elysia.mooc.ops.domain.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.ops.domain.enums.OpsConfigStatus;
import com.elysia.mooc.ops.domain.enums.OpsConfigValueType;
import java.time.LocalDateTime;
import lombok.Data;

/** 系统配置项实体，映射 ops_config_item 表。 */
@Data
@TableName("ops_config_item")
public class OpsConfigItemPO {

    /** 主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置键。 */
    private String configKey;

    /** 配置分组。 */
    private String configGroup;

    /** 配置值，SQL 类型为 LONGTEXT。 */
    private String configValue;

    /** 配置值类型。 */
    private OpsConfigValueType valueType;

    /** 启停状态。 */
    private OpsConfigStatus status;

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
