package com.elysia.mooc.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.teaching.domain.po.TeacherCourseStatPO;
import org.apache.ibatis.annotations.Mapper;

/** 教师课程统计表数据访问接口。 */
@Mapper
public interface TeacherCourseStatMapper extends BaseMapper<TeacherCourseStatPO> {
}
