package com.elysia.mooc.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.statistics.domain.po.UserSearchLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 用户搜索日志 Mapper。 */
@Mapper
public interface UserSearchLogMapper extends BaseMapper<UserSearchLogPO> {
}
