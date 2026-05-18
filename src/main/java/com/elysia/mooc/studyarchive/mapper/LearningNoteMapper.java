package com.elysia.mooc.studyarchive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.studyarchive.domain.po.LearningNotePO;
import org.apache.ibatis.annotations.Mapper;

/** 学习笔记数据访问接口。 */
@Mapper
public interface LearningNoteMapper extends BaseMapper<LearningNotePO> {
}
