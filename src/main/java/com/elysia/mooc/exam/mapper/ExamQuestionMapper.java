package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamQuestionPO;
import org.apache.ibatis.annotations.Mapper;

/** 题目 Mapper。 */
@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestionPO> {
}
