package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamWrongQuestionPO;
import org.apache.ibatis.annotations.Mapper;

/** 错题本 Mapper。 */
@Mapper
public interface ExamWrongQuestionMapper extends BaseMapper<ExamWrongQuestionPO> {
}
