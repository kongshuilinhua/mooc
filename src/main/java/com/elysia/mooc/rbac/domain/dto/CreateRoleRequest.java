package com.elysia.mooc.rbac.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 创建角色请求参数 */
@Data
public class CreateRoleRequest {

    /** 角色编码，全局唯一，如 STUDENT */
    @NotBlank(message = "角色编码不能为空")
    private String code;

    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /** 角色说明 */
    private String description;
}
