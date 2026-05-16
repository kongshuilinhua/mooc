package com.elysia.mooc.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.exam.domain.po.ExamRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 作答记录 Mapper。 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecordPO> {
}
