package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CoursePO;
import org.apache.ibatis.annotations.Mapper;

/** 课程表数据库访问接口。 */
@Mapper
public interface CourseMapper extends BaseMapper<CoursePO> {
}
