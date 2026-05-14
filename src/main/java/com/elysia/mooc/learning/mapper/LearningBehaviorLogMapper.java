package com.elysia.mooc.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.learning.domain.po.LearningBehaviorLogPO;
import org.apache.ibatis.annotations.Mapper;

/** 学习行为日志数据库访问接口。 */
@Mapper
public interface LearningBehaviorLogMapper extends BaseMapper<LearningBehaviorLogPO> {
}
