package com.elysia.mooc.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.rbac.domain.po.SysUserRolePO;
import org.apache.ibatis.annotations.Mapper;

/** 用户角色关系 Mapper */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRolePO> {
}
