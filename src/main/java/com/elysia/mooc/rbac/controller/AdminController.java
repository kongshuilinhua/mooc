package com.elysia.mooc.rbac.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.rbac.domain.dto.CreateRoleRequest;
import com.elysia.mooc.rbac.domain.dto.RbacQuery;
import com.elysia.mooc.rbac.domain.vo.PermissionVO;
import com.elysia.mooc.rbac.domain.vo.RoleVO;
import com.elysia.mooc.rbac.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 角色与权限接口。
 * 所有接口仅限 ADMIN 角色访问。
 */
@Tag(name = "管理端-角色权限")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RbacService rbacService;

    /** 分页查询角色列表，支持关键字和状态筛选 */
    @Operation(summary = "查询角色列表")
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<RoleVO>> listRoles(@Valid RbacQuery query) {
        // 1. 角色管理接口始终返回分页结构，避免同一路径因参数不同产生两种响应格式
        return ApiResult.ok(rbacService.listRoles(query));
    }

    /** 创建新角色，编码不可与已有角色重复 */
    @Operation(summary = "创建角色")
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        // 1. 委托服务层创建角色，封装为统一返回结果
        return ApiResult.ok(rbacService.createRole(request));
    }

    /** 分页查询权限列表，支持关键字和状态筛选 */
    @Operation(summary = "查询权限列表")
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<PermissionVO>> listPermissions(@Valid RbacQuery query) {
        // 1. 权限管理接口始终返回分页结构，与角色列表保持一致的合同格式
        return ApiResult.ok(rbacService.listPermissions(query));
    }
}
