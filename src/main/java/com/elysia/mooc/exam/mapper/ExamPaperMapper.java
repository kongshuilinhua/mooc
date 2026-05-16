package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamPaperPO;
import org.apache.ibatis.annotations.Mapper;

/** 试卷 Mapper。 */
@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaperPO> {
}
