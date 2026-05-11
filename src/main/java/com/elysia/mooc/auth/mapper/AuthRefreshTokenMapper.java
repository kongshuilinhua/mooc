package com.elysia.mooc.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.auth.domain.po.AuthRefreshTokenPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌表数据库访问接口。
 */
@Mapper
public interface AuthRefreshTokenMapper extends BaseMapper<AuthRefreshTokenPO> {
}
