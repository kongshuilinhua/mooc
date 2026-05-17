package com.elysia.mooc.homework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.homework.domain.po.HomeworkSubmissionPO;
import org.apache.ibatis.annotations.Mapper;

/** 作业提交表数据访问接口。 */
@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmissionPO> {
}
