package com.elysia.mooc.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.trade.domain.po.PayRecordPO;
import org.apache.ibatis.annotations.Mapper;

/** 支付记录 Mapper。 */
@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecordPO> {
}
