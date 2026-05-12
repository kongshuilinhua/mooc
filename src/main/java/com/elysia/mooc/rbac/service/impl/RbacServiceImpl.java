package com.elysia.mooc.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import com.elysia.mooc.auth.mapper.SysUserMapper;
import com.elysia.mooc.common.api.PageResult;
import com.elysia.mooc.common.enums.EnableStatus;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.rbac.constants.RbacErrorCode;
import com.elysia.mooc.rbac.domain.dto.CreateRoleRequest;
import com.elysia.mooc.rbac.domain.dto.RbacQuery;
import com.elysia.mooc.rbac.domain.po.SysPermissionPO;
import com.elysia.mooc.rbac.domain.po.SysRolePO;
import com.elysia.mooc.rbac.domain.po.SysRolePermissionPO;
import com.elysia.mooc.rbac.domain.po.SysUserRolePO;
import com.elysia.mooc.rbac.domain.vo.PermissionVO;
import com.elysia.mooc.rbac.domain.vo.RoleVO;
import com.elysia.mooc.rbac.mapper.SysPermissionMapper;
import com.elysia.mooc.rbac.mapper.SysRoleMapper;
import com.elysia.mooc.rbac.mapper.SysRolePermissionMapper;
import com.elysia.mooc.rbac.mapper.SysUserRoleMapper;
import com.elysia.mooc.rbac.service.RbacService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC 核心服务实现。
 */
@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 查询用户拥有的角色编码列表。
     * @param userId 用户ID
     * @return 角色编码列表
     */
    @Override
    public List<String> getUserRoleCodes(Long userId) {
        // 1. 空值直接返回空列表，避免后续查询无意义执行
        if (userId == null) {
            return Collections.emptyList();
        }
        // 2. 内置管理员账号即便关系表被误改，也强制补齐 ADMIN 角色，避免管理端被锁死
        if (isBuiltinAdmin(userId)) {
            LinkedHashSet<String> roleCodes = new LinkedHashSet<>(loadRoleCodes(userId));
            roleCodes.add("ADMIN");
            return List.copyOf(roleCodes);
        }
        // 3. 普通用户按关系表返回启用中的角色编码
        return loadRoleCodes(userId);
    }

    /**
     * 查询用户拥有的权限编码列表。
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        // 1. 空值直接返回空列表
        if (userId == null) {
            return Collections.emptyList();
        }
        // 2. 内置管理员账号始终拥有全部启用权限，避免误删 ADMIN 关系后整个后台不可用
        if (isBuiltinAdmin(userId)) {
            return sysPermissionMapper.selectList(
                            Wrappers.<SysPermissionPO>lambdaQuery().eq(SysPermissionPO::getStatus, EnableStatus.ENABLED))
                    .stream()
                    .map(SysPermissionPO::getCode)
                    .distinct()
                    .collect(Collectors.toList());
        }
        // 3. 先只加载启用中的角色，防止禁用角色继续透出权限
        Set<Long> roleIds = loadEnabledRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 4. 再按启用角色加载启用权限集合
        List<SysRolePermissionPO> rolePermissions = sysRolePermissionMapper.selectList(
                Wrappers.<SysRolePermissionPO>lambdaQuery().in(SysRolePermissionPO::getRoleId, roleIds));
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermissionPO::getPermissionId)
                .collect(Collectors.toSet());
        return sysPermissionMapper.selectList(
                        Wrappers.<SysPermissionPO>lambdaQuery()
                                .in(SysPermissionPO::getId, permissionIds)
                                .eq(SysPermissionPO::getStatus, EnableStatus.ENABLED))
                .stream()
                .map(SysPermissionPO::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 一次性加载用户角色与权限，用于登录态构建。
     * @param userId 用户ID
     * @return 角色和权限映射
     */
    @Override
    public Map<String, List<String>> loadUserRolesAndPermissions(Long userId) {
        Map<String, List<String>> result = new HashMap<>();
        result.put("roles", getUserRoleCodes(userId));
        result.put("permissions", getUserPermissionCodes(userId));
        return result;
    }

    /**
     * 分页查询角色列表。
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<RoleVO> listRoles(RbacQuery query) {
        // 1. 按关键字和状态拼装分页查询条件
        LambdaQueryWrapper<SysRolePO> wrapper = Wrappers.<SysRolePO>lambdaQuery();
        if (query.getStatus() != null) {
            wrapper.eq(SysRolePO::getStatus, query.getStatus());
        } else {
            wrapper.eq(SysRolePO::getStatus, EnableStatus.ENABLED);
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(SysRolePO::getCode, query.getKeyword())
                    .or().like(SysRolePO::getName, query.getKeyword()));
        }
        wrapper.orderByAsc(SysRolePO::getId);
        // 2. 执行分页查询并转换为前端视图对象
        Page<SysRolePO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<SysRolePO> result = sysRoleMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toRoleVO);
    }

    /**
     * 查询全部角色列表，用于弹窗和下拉框。
     * @return 全量启用角色
     */
    @Override
    public List<RoleVO> listAllRoles() {
        // 1. 只返回启用中的角色，避免前端把禁用角色展示为可选项
        return sysRoleMapper.selectList(Wrappers.<SysRolePO>lambdaQuery()
                        .eq(SysRolePO::getStatus, EnableStatus.ENABLED)
                        .orderByAsc(SysRolePO::getId))
                .stream()
                .map(this::toRoleVO)
                .collect(Collectors.toList());
    }

    /**
     * 创建角色。
     * @param request 创建请求
     * @return 新角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(CreateRoleRequest request) {
        // 1. 先校验角色编码唯一性，避免后续写入冲突
        Long count = sysRoleMapper.selectCount(
                Wrappers.<SysRolePO>lambdaQuery().eq(SysRolePO::getCode, request.getCode()));
        if (count > 0) {
            throw new BizException(RbacErrorCode.ROLE_CODE_EXISTS);
        }
        // 2. 按默认启用状态创建角色
        SysRolePO role = new SysRolePO();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(EnableStatus.ENABLED);
        sysRoleMapper.insert(role);
        return toRoleVO(role);
    }

    /**
     * 分页查询权限列表。
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<PermissionVO> listPermissions(RbacQuery query) {
        // 1. 按状态和关键字构建分页条件
        LambdaQueryWrapper<SysPermissionPO> wrapper = Wrappers.<SysPermissionPO>lambdaQuery();
        if (query.getStatus() != null) {
            wrapper.eq(SysPermissionPO::getStatus, query.getStatus());
        } else {
            wrapper.eq(SysPermissionPO::getStatus, EnableStatus.ENABLED);
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(SysPermissionPO::getCode, query.getKeyword())
                    .or().like(SysPermissionPO::getName, query.getKeyword()));
        }
        wrapper.orderByAsc(SysPermissionPO::getSort);
        // 2. 查询后转换成权限视图对象
        Page<SysPermissionPO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<SysPermissionPO> result = sysPermissionMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toPermissionVO);
    }

    /**
     * 查询全部权限列表，用于轻量前端场景。
     * @return 全量启用权限
     */
    @Override
    public List<PermissionVO> listAllPermissions() {
        // 1. 按排序号和主键升序返回，确保前端展示顺序稳定
        return sysPermissionMapper.selectList(Wrappers.<SysPermissionPO>lambdaQuery()
                        .eq(SysPermissionPO::getStatus, EnableStatus.ENABLED)
                        .orderByAsc(SysPermissionPO::getSort)
                        .orderByAsc(SysPermissionPO::getId))
                .stream()
                .map(this::toPermissionVO)
                .collect(Collectors.toList());
    }

    /**
     * 将角色实体转换为角色视图对象。
     * @param po 角色实体
     * @return 角色视图对象
     */
    private RoleVO toRoleVO(SysRolePO po) {
        return RoleVO.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .description(po.getDescription())
                .status(po.getStatus())
                .build();
    }

    /**
     * 将权限实体转换为权限视图对象。
     * @param po 权限实体
     * @return 权限视图对象
     */
    private PermissionVO toPermissionVO(SysPermissionPO po) {
        return PermissionVO.builder()
                .id(po.getId())
                .code(po.getCode())
                .name(po.getName())
                .type(po.getType())
                .parentId(po.getParentId())
                .path(po.getPath())
                .sort(po.getSort())
                .status(po.getStatus())
                .build();
    }

    /**
     * 读取用户在关系表中的启用角色编码。
     * @param userId 用户ID
     * @return 角色编码列表
     */
    private List<String> loadRoleCodes(Long userId) {
        // 1. 先收集用户已绑定且启用的角色ID，再统一批量查询角色编码
        Set<Long> roleIds = loadEnabledRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectList(
                        Wrappers.<SysRolePO>lambdaQuery()
                                .in(SysRolePO::getId, roleIds)
                                .eq(SysRolePO::getStatus, EnableStatus.ENABLED))
                .stream()
                .map(SysRolePO::getCode)
                .collect(Collectors.toList());
    }

    /**
     * 查询用户绑定的启用角色ID集合。
     * @param userId 用户ID
     * @return 启用角色ID集合
     */
    private Set<Long> loadEnabledRoleIds(Long userId) {
        // 1. 用户角色关系表本身无状态字段，需要关联角色表过滤禁用角色
        List<SysUserRolePO> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRolePO>lambdaQuery().eq(SysUserRolePO::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        return sysRoleMapper.selectList(
                        Wrappers.<SysRolePO>lambdaQuery()
                                .in(SysRolePO::getId, userRoles.stream()
                                        .map(SysUserRolePO::getRoleId)
                                        .collect(Collectors.toSet()))
                                .eq(SysRolePO::getStatus, EnableStatus.ENABLED))
                .stream()
                .map(SysRolePO::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 判断用户是否为系统内置管理员账号。
     * @param userId 用户ID
     * @return true 表示内置管理员
     */
    private boolean isBuiltinAdmin(Long userId) {
        // 1. 仅对未删除的 admin 账号启用兜底，避免影响普通用户权限模型
        SysUserPO user = sysUserMapper.selectById(userId);
        return user != null
                && user.getDeleted() != null
                && user.getDeleted() == SysUserPO.DELETED_NO
                && "admin".equalsIgnoreCase(user.getUsername());
    }
}
