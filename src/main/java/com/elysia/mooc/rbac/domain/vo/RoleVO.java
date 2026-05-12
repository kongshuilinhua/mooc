package com.elysia.mooc.rbac.domain.vo;

import com.elysia.mooc.common.enums.EnableStatus;
import lombok.Builder;
import lombok.Data;

/** 角色视图对象，用于接口返回 */
@Data
@Builder
public class RoleVO {

    /** 角色ID */
    private Long id;

    /** 角色编码 */
    private String code;

    /** 角色名称 */
    private String name;

    /** 角色说明 */
    private String description;

    /** 状态：0 禁用，1 启用 */
    private EnableStatus status;
}
