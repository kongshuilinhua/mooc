package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseTagPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程标签表数据库访问接口。 */
@Mapper
public interface CourseTagMapper extends BaseMapper<CourseTagPO> {
}
