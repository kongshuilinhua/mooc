package com.elysia.mooc.rbac.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 角色实体，映射 sys_role 表。
 * 存储平台角色（学生、教师、管理员），角色是权限的集合。
 */
@Data
@TableName("sys_role")
public class SysRolePO {

    /** 角色ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码，如 STUDENT/TEACHER/ADMIN，唯一标识 */
    private String code;

    /** 角色名称，如 学生/教师/管理员 */
    private String name;

    /** 角色说明 */
    private String description;

    /** 状态：0 禁用，1 启用 */
    private EnableStatus status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
