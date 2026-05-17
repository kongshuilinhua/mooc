package com.elysia.mooc.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.trade.domain.po.TradeOrderPO;
import org.apache.ibatis.annotations.Mapper;

/** 订单 Mapper。 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrderPO> {
}
