package com.elysia.mooc.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.rbac.domain.po.SysRolePO;
import org.apache.ibatis.annotations.Mapper;

/** 角色 Mapper，提供 sys_role 表的基础 CRUD 操作 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRolePO> {
}
