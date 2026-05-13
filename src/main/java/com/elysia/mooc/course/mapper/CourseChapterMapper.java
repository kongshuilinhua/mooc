package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseChapterPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程章节数据库访问接口。 */
@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapterPO> {
}
