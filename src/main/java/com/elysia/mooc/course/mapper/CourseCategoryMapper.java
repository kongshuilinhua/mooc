package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseCategoryPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程分类表数据库访问接口。 */
@Mapper
public interface CourseCategoryMapper extends BaseMapper<CourseCategoryPO> {
}
