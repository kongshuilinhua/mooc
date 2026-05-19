package com.elysia.mooc.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.task.domain.po.StudyTaskInstancePO;
import org.apache.ibatis.annotations.Mapper;

/** 学习任务实例 Mapper。 */
@Mapper
public interface StudyTaskInstanceMapper extends BaseMapper<StudyTaskInstancePO> {
}
