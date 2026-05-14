package com.elysia.mooc.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.course.domain.po.CourseAuditLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 课程审核日志数据库访问接口。 */
@Mapper
public interface CourseAuditLogMapper extends BaseMapper<CourseAuditLogPO> {
}
