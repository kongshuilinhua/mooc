package com.elysia.mooc.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.task.domain.po.StudyTaskPlanPO;
import org.apache.ibatis.annotations.Mapper;

/** 学习任务计划 Mapper。 */
@Mapper
public interface StudyTaskPlanMapper extends BaseMapper<StudyTaskPlanPO> {
}
