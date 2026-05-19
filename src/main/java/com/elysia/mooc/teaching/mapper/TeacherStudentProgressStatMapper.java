package com.elysia.mooc.teaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.teaching.domain.po.TeacherStudentProgressStatPO;
import org.apache.ibatis.annotations.Mapper;

/** 教师学员进度统计表数据访问接口。 */
@Mapper
public interface TeacherStudentProgressStatMapper extends BaseMapper<TeacherStudentProgressStatPO> {
}
