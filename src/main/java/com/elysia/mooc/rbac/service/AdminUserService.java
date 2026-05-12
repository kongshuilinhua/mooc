package com.elysia.mooc.rbac.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.rbac.domain.dto.AssignUserRolesRequest;
import com.elysia.mooc.rbac.domain.dto.UpdateUserStatusRequest;
import com.elysia.mooc.rbac.domain.dto.UserQuery;
import com.elysia.mooc.rbac.domain.vo.AdminUserVO;

/**
 * 管理员用户管理服务。
 */
public interface AdminUserService {

    /** 分页查询用户列表，含角色信息 */
    PageResult<AdminUserVO> listUsers(UserQuery query);

    /** 修改用户的启用/禁用状态 */
    AdminUserVO updateUserStatus(Long userId, UpdateUserStatusRequest request);

    /** 为用户分配角色（全量替换，角色变更后需重新登录） */
    AdminUserVO assignUserRoles(Long userId, AssignUserRolesRequest request);
}