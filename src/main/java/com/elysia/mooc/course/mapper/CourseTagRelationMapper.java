package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseTagRelationPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程标签关系表数据库访问接口。 */
@Mapper
public interface CourseTagRelationMapper extends BaseMapper<CourseTagRelationPO> {
}
