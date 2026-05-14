package com.elysia.mooc.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.interaction.domain.po.CourseRatingPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程评价表数据库访问接口。 */
@Mapper
public interface CourseRatingMapper extends BaseMapper<CourseRatingPO> {
}
