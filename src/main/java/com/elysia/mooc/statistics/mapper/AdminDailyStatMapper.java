package com.elysia.mooc.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.statistics.domain.po.AdminDailyStatPO;
import org.apache.ibatis.annotations.Mapper;

/** 后台每日统计 Mapper。 */
@Mapper
public interface AdminDailyStatMapper extends BaseMapper<AdminDailyStatPO> {
}
