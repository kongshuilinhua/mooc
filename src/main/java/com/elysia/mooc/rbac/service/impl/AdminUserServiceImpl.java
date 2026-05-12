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
import com.elysia.mooc.rbac.domain.dto.AssignUserRolesRequest;
import com.elysia.mooc.rbac.domain.dto.UpdateUserStatusRequest;
import com.elysia.mooc.rbac.domain.dto.UserQuery;
import com.elysia.mooc.rbac.domain.po.SysRolePO;
import com.elysia.mooc.rbac.domain.po.SysUserRolePO;
import com.elysia.mooc.rbac.domain.vo.AdminUserVO;
import com.elysia.mooc.rbac.mapper.SysRoleMapper;
import com.elysia.mooc.rbac.mapper.SysUserRoleMapper;
import com.elysia.mooc.rbac.service.AdminUserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 管理员用户管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    /** 分页查询用户列表，含角色信息 */
    @Override
    public PageResult<AdminUserVO> listUsers(UserQuery query) {
        // 1. 构建查询条件：排除已删除用户
        LambdaQueryWrapper<SysUserPO> wrapper = Wrappers.<SysUserPO>lambdaQuery()
                .eq(SysUserPO::getDeleted, SysUserPO.DELETED_NO);
        // 1.1 关键字搜索：匹配用户名、昵称、邮箱
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysUserPO::getUsername, query.getKeyword())
                    .or()
                    .like(SysUserPO::getNickname, query.getKeyword())
                    .or()
                    .like(SysUserPO::getEmail, query.getKeyword()));
        }
        // 1.2 状态筛选
        if (query.getStatus() != null) {
            wrapper.eq(SysUserPO::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysUserPO::getCreateTime);
        // 2. 执行分页查询
        Page<SysUserPO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<SysUserPO> result = sysUserMapper.selectPage(page, wrapper);
        // 3. 批量加载角色信息
        List<SysUserPO> users = result.getRecords();
        List<AdminUserVO> records = Collections.emptyList();
        if (!users.isEmpty()) {
            // 3.1 收集当前页所有用户ID
            Set<Long> userIds = users.stream().map(SysUserPO::getId).collect(Collectors.toSet());
            // 3.2 一次查询获取所有用户的角色关系
            List<SysUserRolePO> allUserRoles = sysUserRoleMapper.selectList(
                    Wrappers.<SysUserRolePO>lambdaQuery().in(SysUserRolePO::getUserId, userIds));
            // 3.3 构建 userId -> roleId列表 的映射
            Map<Long, List<Long>> userRoleMap = allUserRoles.stream()
                    .collect(Collectors.groupingBy(SysUserRolePO::getUserId,
                            Collectors.mapping(SysUserRolePO::getRoleId, Collectors.toList())));
            // 3.4 一次查询获取所有涉及的角色编码
            Set<Long> allRoleIds = allUserRoles.stream().map(SysUserRolePO::getRoleId).collect(Collectors.toSet());
            List<SysRolePO> roles = allRoleIds.isEmpty() ? Collections.emptyList()
                    : sysRoleMapper.selectList(Wrappers.<SysRolePO>lambdaQuery()
                            .in(SysRolePO::getId, allRoleIds)
                            .eq(SysRolePO::getStatus, EnableStatus.ENABLED));
            Map<Long, String> roleIdCodeMap = roles.stream()
                    .collect(Collectors.toMap(SysRolePO::getId, SysRolePO::getCode));
            // 3.5 组装每个用户的 AdminUserVO
            records = new ArrayList<>();
            for (SysUserPO user : users) {
                List<Long> userRoleIds = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
                List<String> roleCodes = userRoleIds.stream()
                        .map(roleIdCodeMap::get).filter(Objects::nonNull).collect(Collectors.toList());
                records.add(AdminUserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .status(user.getStatus())
                        .roleCodes(roleCodes)
                        .roles(roleCodes)
                        .lastLoginTime(user.getLastLoginTime())
                        .lastLoginIp(user.getLastLoginIp())
                        .createTime(user.getCreateTime())
                        .build());
            }
        }
        return PageResult.of(result, records);
    }

    /** 修改用户的启用/禁用状态 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        // 1. 查询用户是否存在
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(RbacErrorCode.USER_NOT_FOUND);
        }
        // 2. 更新状态并落库
        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
        // 3. 返回更新后的用户信息
        return toAdminUserVO(user);
    }

    /** 为用户分配角色（全量替换） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO assignUserRoles(Long userId, AssignUserRolesRequest request) {
        // 1. 查询用户是否存在
        SysUserPO user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(RbacErrorCode.USER_NOT_FOUND);
        }
        // 2. 兼容角色ID和角色编码两种提交方式，统一解析成角色实体
        List<SysRolePO> roles = resolveAssignRoles(request);
        // 2.1 内置管理员账号必须始终保留 ADMIN 角色，避免后台把自己锁死
        ensureBuiltinAdminRole(user, roles);
        // 3. 先删除用户原有角色，再批量插入新角色
        sysUserRoleMapper.delete(
                Wrappers.<SysUserRolePO>lambdaQuery().eq(SysUserRolePO::getUserId, userId));
        for (SysRolePO role : roles) {
            SysUserRolePO userRole = new SysUserRolePO();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
        // 4. 返回更新后的用户信息
        return toAdminUserVO(user);
    }

    /**
     * 兼容前端提交角色ID或角色编码，统一解析成可落库的角色实体。
     * @param request 分配角色请求
     * @return 已校验通过的角色实体列表
     */
    private List<SysRolePO> resolveAssignRoles(AssignUserRolesRequest request) {
        // 1. 优先使用 roleIds，兼容未来管理端直接提交主键的场景
        if (!CollectionUtils.isEmpty(request.getRoleIds())) {
            List<SysRolePO> roles = sysRoleMapper.selectBatchIds(request.getRoleIds());
            if (roles.size() != request.getRoleIds().size()
                    || roles.stream().anyMatch(role -> EnableStatus.ENABLED != role.getStatus())) {
                throw new BizException(RbacErrorCode.ROLE_INVALID);
            }
            return roles;
        }
        // 2. 未传 roleIds 时，退化为按 roleCodes 查询，兼容当前前端实现
        if (!CollectionUtils.isEmpty(request.getRoleCodes())) {
            List<String> roleCodes = request.getRoleCodes().stream()
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
            if (roleCodes.isEmpty()) {
                throw new BizException(RbacErrorCode.ROLE_INVALID);
            }
            List<SysRolePO> roles = sysRoleMapper.selectList(
                    Wrappers.<SysRolePO>lambdaQuery()
                            .in(SysRolePO::getCode, roleCodes)
                            .eq(SysRolePO::getStatus, EnableStatus.ENABLED));
            if (roles.size() != roleCodes.size()) {
                throw new BizException(RbacErrorCode.ROLE_INVALID);
            }
            return roles;
        }
        // 3. 两种字段都没传时，按原业务规则拒绝请求
        throw new BizException(RbacErrorCode.ROLE_INVALID);
    }

    /**
     * 保护内置管理员账号始终持有 ADMIN 角色。
     * @param user 当前分配角色的用户
     * @param roles 本次准备写入的角色列表
     */
    private void ensureBuiltinAdminRole(SysUserPO user, List<SysRolePO> roles) {
        // 1. 只对系统内置 admin 账号做保护，避免普通账号误受影响
        if (!"admin".equalsIgnoreCase(user.getUsername())) {
            return;
        }
        boolean hasAdminRole = roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getCode()));
        if (!hasAdminRole) {
            throw new BizException(RbacErrorCode.PERMISSION_DENIED, "内置管理员账号必须保留管理员角色");
        }
    }

    /** 将用户PO转换为AdminUserVO，含角色编码 */
    private AdminUserVO toAdminUserVO(SysUserPO user) {
        // 1. 查询用户角色关系
        List<SysUserRolePO> userRoles = sysUserRoleMapper.selectList(
                Wrappers.<SysUserRolePO>lambdaQuery().eq(SysUserRolePO::getUserId, user.getId()));
        List<String> roleCodes = Collections.emptyList();
        // 2. 有角色时查询角色编码
        if (!userRoles.isEmpty()) {
            Set<Long> roleIds = userRoles.stream().map(SysUserRolePO::getRoleId).collect(Collectors.toSet());
            roleCodes = sysRoleMapper.selectList(
                    Wrappers.<SysRolePO>lambdaQuery()
                            .in(SysRolePO::getId, roleIds)
                            .eq(SysRolePO::getStatus, EnableStatus.ENABLED))
                    .stream().map(SysRolePO::getCode).collect(Collectors.toList());
        }
        // 3. 构建VO
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roleCodes(roleCodes)
                .roles(roleCodes)
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createTime(user.getCreateTime())
                .build();
    }
}
