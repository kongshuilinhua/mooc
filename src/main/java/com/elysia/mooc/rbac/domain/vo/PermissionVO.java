package com.elysia.mooc.rbac.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.enums.PermissionType;
import lombok.Builder;
import lombok.Data;

/** 权限视图对象，用于接口返回 */
@Data
@Builder
public class PermissionVO {

    /** 权限ID */
    private Long id;

    /** 权限编码 */
    private String code;

    /** 权限名称 */
    private String name;

    /** 权限类型：MENU/BUTTON/API */
    private PermissionType type;

    /** 父权限ID，0 表示顶级 */
    private Long parentId;

    /** 前端路由或接口路径 */
    private String path;

    /** 排序号 */
    private Integer sort;

    /** 状态：0 禁用，1 启用 */
    private EnableStatus status;
}
