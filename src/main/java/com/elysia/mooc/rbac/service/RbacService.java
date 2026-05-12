package com.elysia.mooc.rbac.service;

import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.rbac.domain.dto.CreateRoleRequest;
import com.elysia.mooc.rbac.domain.dto.RbacQuery;
import com.elysia.mooc.rbac.domain.vo.PermissionVO;
import com.elysia.mooc.rbac.domain.vo.RoleVO;
import java.util.List;
import java.util.Map;

/**
 * RBAC 核心服务：角色与权限管理。
 */
public interface RbacService {

    /** 查询用户拥有的角色编码列表 */
    List<String> getUserRoleCodes(Long userId);

    /** 查询用户拥有的权限编码列表 */
    List<String> getUserPermissionCodes(Long userId);

    /** 一次性加载用户的角色和权限，用于JWT过滤器填充SecurityContext */
    Map<String, List<String>> loadUserRolesAndPermissions(Long userId);

    /** 分页查询角色列表 */
    PageResult<RoleVO> listRoles(RbacQuery query);

    /** 查询全部角色列表，用于下拉框、勾选框等轻量选择场景 */
    List<RoleVO> listAllRoles();

    /** 创建新角色 */
    RoleVO createRole(CreateRoleRequest request);

    /** 分页查询权限列表 */
    PageResult<PermissionVO> listPermissions(RbacQuery query);

    /** 查询全部权限列表，用于前端轻量选择场景 */
    List<PermissionVO> listAllPermissions();
}
