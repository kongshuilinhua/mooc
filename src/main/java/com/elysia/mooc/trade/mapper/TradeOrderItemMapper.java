package com.elysia.mooc.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elysia.mooc.trade.domain.po.TradeOrderItemPO;
import org.apache.ibatis.annotations.Mapper;

/** 订单明细 Mapper。 */
@Mapper
public interface TradeOrderItemMapper extends BaseMapper<TradeOrderItemPO> {
}
