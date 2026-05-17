package com.elysia.mooc.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.statistics.domain.po.CourseHotStatsPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程热度统计 Mapper。 */
@Mapper
public interface CourseHotStatsMapper extends BaseMapper<CourseHotStatsPO> {
}
