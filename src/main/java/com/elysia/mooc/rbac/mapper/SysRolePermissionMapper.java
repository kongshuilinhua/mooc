package com.elysia.mooc.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.rbac.domain.po.SysRolePermissionPO;
import org.apache.ibatis.annotations.Mapper;

/** 角色权限关系 Mapper */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermissionPO> {
}
