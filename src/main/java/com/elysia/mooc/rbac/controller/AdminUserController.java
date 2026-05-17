package com.elysia.mooc.rbac.controller;

import com.elysia.mooc.common.api.ApiResult;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.audit.AuditLog;
import com.elysia.mooc.common.validate.ParamChecker;
import com.elysia.mooc.rbac.domain.dto.AssignUserRolesRequest;
import com.elysia.mooc.rbac.domain.dto.UpdateUserStatusRequest;
import com.elysia.mooc.rbac.domain.dto.UserQuery;
import com.elysia.mooc.rbac.domain.vo.AdminUserVO;
import com.elysia.mooc.rbac.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 - 用户管理接口。
 * 提供用户列表、状态变更、角色分配，仅限 ADMIN 访问。
 */
@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 分页查询用户列表，含角色信息，支持关键字和状态筛选 */
    @Operation(summary = "查询用户列表")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<PageResult<AdminUserVO>> listUsers(@Valid UserQuery query) {
        // 1. 委托服务层分页查询用户，封装为统一返回结果
        return ApiResult.ok(adminUserService.listUsers(query));
    }

    /** 修改用户启用/禁用状态，userId 从路径参数获取 */
    @Operation(summary = "修改用户状态")
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "USER_STATUS_UPDATE", targetType = "USER", targetId = "#userId")
    public ApiResult<AdminUserVO> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        // 1. 委托服务层修改用户状态，封装为统一返回结果
        return ApiResult.ok(adminUserService.updateUserStatus(userId, request));
    }

    /** 分配用户角色，先删后插全量替换，userId 从路径参数获取 */
    @Operation(summary = "分配用户角色")
    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @ParamChecker
    @AuditLog(action = "USER_ROLE_ASSIGN", targetType = "USER", targetId = "#userId")
    public ApiResult<AdminUserVO> assignUserRoles(
            @PathVariable Long userId,
            @Valid @RequestBody AssignUserRolesRequest request) {
        // 1. 委托服务层分配用户角色，封装为统一返回结果
        return ApiResult.ok(adminUserService.assignUserRoles(userId, request));
    }
}
