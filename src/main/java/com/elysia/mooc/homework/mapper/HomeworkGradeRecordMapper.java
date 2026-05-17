package com.elysia.mooc.homework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.homework.domain.po.HomeworkGradeRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 作业批改记录表数据访问接口。 */
@Mapper
public interface HomeworkGradeRecordMapper extends BaseMapper<HomeworkGradeRecordPO> {
}
