package com.elysia.mooc.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.teaching.domain.po.TeacherRevenueStatPO;
import org.apache.ibatis.annotations.Mapper;

/** 教师收入统计表数据访问接口。 */
@Mapper
public interface TeacherRevenueStatMapper extends BaseMapper<TeacherRevenueStatPO> {
}
