package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamQuestionOptionPO;
import org.apache.ibatis.annotations.Mapper;

/** 题目选项 Mapper。 */
@Mapper
public interface ExamQuestionOptionMapper extends BaseMapper<ExamQuestionOptionPO> {
}
