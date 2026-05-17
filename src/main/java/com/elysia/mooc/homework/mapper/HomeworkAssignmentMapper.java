package com.elysia.mooc.homework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.homework.domain.po.HomeworkAssignmentPO;
import org.apache.ibatis.annotations.Mapper;

/** 作业主表数据访问接口。 */
@Mapper
public interface HomeworkAssignmentMapper extends BaseMapper<HomeworkAssignmentPO> {
}
