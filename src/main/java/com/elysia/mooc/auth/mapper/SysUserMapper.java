package com.elysia.mooc.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.auth.domain.po.SysUserPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据库访问接口。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserPO> {
}
