package com.elysia.mooc.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.learning.domain.po.LearningRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 小节学习记录数据库访问接口。 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecordPO> {
}
