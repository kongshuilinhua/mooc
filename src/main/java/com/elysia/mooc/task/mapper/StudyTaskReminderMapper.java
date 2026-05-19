package com.elysia.mooc.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.task.domain.po.StudyTaskReminderPO;
import org.apache.ibatis.annotations.Mapper;

/** 学习任务提醒 Mapper。 */
@Mapper
public interface StudyTaskReminderMapper extends BaseMapper<StudyTaskReminderPO> {
}
