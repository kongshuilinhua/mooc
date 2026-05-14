package com.elysia.mooc.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import org.apache.ibatis.annotations.Mapper;

/** 我的课程数据库访问接口。 */
@Mapper
public interface LearningCourseMapper extends BaseMapper<LearningCoursePO> {
}
