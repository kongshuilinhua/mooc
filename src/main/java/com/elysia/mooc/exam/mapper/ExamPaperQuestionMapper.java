package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamPaperQuestionPO;
import org.apache.ibatis.annotations.Mapper;

/** 试卷题目关系 Mapper。 */
@Mapper
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestionPO> {
}
