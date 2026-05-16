package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamAnswerRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 答案记录 Mapper。 */
@Mapper
public interface ExamAnswerRecordMapper extends BaseMapper<ExamAnswerRecordPO> {
}
