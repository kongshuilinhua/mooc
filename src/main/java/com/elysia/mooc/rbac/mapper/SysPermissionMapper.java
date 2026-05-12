package com.elysia.mooc.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.rbac.domain.po.SysPermissionPO;
import org.apache.ibatis.annotations.Mapper;

/** 权限 Mapper，提供 sys_permission 表的基础 CRUD 操作 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermissionPO> {
}
