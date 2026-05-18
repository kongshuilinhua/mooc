package com.elysia.mooc.studyarchive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.studyarchive.domain.po.LearningReportPO;
import org.apache.ibatis.annotations.Mapper;

/** 学习报告数据访问接口。 */
@Mapper
public interface LearningReportMapper extends BaseMapper<LearningReportPO> {
}
