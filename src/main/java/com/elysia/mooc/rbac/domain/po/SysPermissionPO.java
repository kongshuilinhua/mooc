package com.elysia.mooc.rbac.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.enums.PermissionType;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 权限实体，映射 sys_permission 表。
 * 区分菜单权限（前端路由）、按钮权限（页面操作）、API权限（接口鉴权）。
 */
@Data
@TableName("sys_permission")
public class SysPermissionPO {

    /** 权限ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限编码，如 course:view，全局唯一 */
    private String code;

    /** 权限名称 */
    private String name;

    /** 权限类型：MENU 菜单、BUTTON 按钮、API 接口 */
    private PermissionType type;

    /** 父权限ID，0 表示顶级权限，用于构建权限树 */
    private Long parentId;

    /** 前端路由路径或接口路径 */
    private String path;

    /** 排序号，值越小越靠前 */
    private Integer sort;

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
