package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseConceptPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程知识点数据库访问接口。 */
@Mapper
public interface CourseConceptMapper extends BaseMapper<CourseConceptPO> {
}
