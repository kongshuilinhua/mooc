package com.elysia.mooc.studyarchive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.studyarchive.domain.po.LearningWrongBookPO;
import org.apache.ibatis.annotations.Mapper;

/** 错题本数据访问接口。 */
@Mapper
public interface LearningWrongBookMapper extends BaseMapper<LearningWrongBookPO> {
}
