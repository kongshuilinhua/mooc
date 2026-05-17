package com.elysia.mooc.trade.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.learning.domain.po.LearningCoursePO;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.trade.constants.TradeConstants;
import com.elysia.mooc.trade.constants.TradeErrorCode;
import com.elysia.mooc.trade.domain.dto.MockPayRequest;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import com.elysia.mooc.trade.domain.enums.PayChannel;
import com.elysia.mooc.trade.domain.enums.PayStatus;
import com.elysia.mooc.trade.domain.po.PayRecordPO;
import com.elysia.mooc.trade.domain.po.TradeOrderItemPO;
import com.elysia.mooc.trade.domain.po.TradeOrderPO;
import com.elysia.mooc.trade.domain.vo.PayResultVO;
import com.elysia.mooc.trade.mapper.PayRecordMapper;
import com.elysia.mooc.trade.mapper.TradeOrderItemMapper;
import com.elysia.mooc.trade.mapper.TradeOrderMapper;
import com.elysia.mooc.trade.service.OrderNoGenerator;
import com.elysia.mooc.trade.service.PayService;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 支付服务实现。 */
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final UserContextService userContextService;
    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final PayRecordMapper payRecordMapper;
    private final LearningCourseMapper learningCourseMapper;
    private final OrderNoGenerator orderNoGenerator;
    private final BusinessEventPublisher businessEventPublisher;
    private final OrderPaidEventConsumer orderPaidEventConsumer;

    /**
     * 模拟支付订单。
     *
     * @param orderId 订单 ID
     * @param request 模拟支付请求
     * @return 支付结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResultVO mockPay(Long orderId, MockPayRequest request) {
        Long userId = userContextService.currentUserId();
        TradeOrderPO order = requireOrder(orderId);
        checkOwner(order, userId);
        if (order.getStatus() == OrderStatus.PAID) {
            return paidResult(order, findSuccessPayRecord(order.getId()), hasJoinedCourse(userId, firstCourseId(order)));
        }
        validatePayable(order);

        LocalDateTime payTime = LocalDateTime.now();
        PayChannel payChannel = resolvePayChannel(request);
        PayRecordPO payRecord = buildSuccessPayRecord(order, payChannel, payTime);
        payRecordMapper.insert(payRecord);

        order.setStatus(OrderStatus.PAID);
        order.setPayTime(payTime);
        tradeOrderMapper.updateById(order);

        TradeOrderItemPO item = requireSingleItem(order.getId());
        DomainEvent event = businessEventPublisher.publishTradeOrderPaid(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                item.getCourseId(),
                order.getPayAmount(),
                payRecord.getPayNo(),
                payTime);
        // day21 的加课属于核心验收链路，除 Kafka 投递外也在本地幂等消费一次，避免异步延迟影响支付结果。
        orderPaidEventConsumer.consumeOrderPaid(event);

        return paidResult(order, payRecord, true);
    }

    private TradeOrderPO requireOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_NOT_FOUND);
        }
        TradeOrderPO order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_NOT_FOUND);
        }
        return order;
    }

    private void checkOwner(TradeOrderPO order, Long userId) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_FORBIDDEN);
        }
    }

    private void validatePayable(TradeOrderPO order) {
        if (order.getStatus() != OrderStatus.UNPAID) {
            throw new BizException(TradeErrorCode.TRADE_ORDER_STATUS_INVALID, "当前订单不能支付");
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            order.setStatus(OrderStatus.CLOSED);
            tradeOrderMapper.updateById(order);
            throw new BizException(TradeErrorCode.TRADE_ORDER_STATUS_INVALID, "订单已超时关闭，不能继续支付");
        }
    }

    private PayRecordPO buildSuccessPayRecord(TradeOrderPO order, PayChannel payChannel, LocalDateTime payTime) {
        PayRecordPO payRecord = new PayRecordPO();
        payRecord.setPayNo(orderNoGenerator.nextPayNo());
        payRecord.setOrderId(order.getId());
        payRecord.setOrderNo(order.getOrderNo());
        payRecord.setPayChannel(payChannel);
        payRecord.setAmount(order.getPayAmount());
        payRecord.setStatus(PayStatus.SUCCESS);
        payRecord.setCallbackPayload("{\"channel\":\"" + payChannel.getValue() + "\",\"tradeStatus\":\"SUCCESS\"}");
        payRecord.setPayTime(payTime);
        payRecord.setDeleted(0);
        return payRecord;
    }

    private PayChannel resolvePayChannel(MockPayRequest request) {
        if (request == null || !StringUtils.hasText(request.getPayChannel())) {
            return PayChannel.MOCK;
        }
        return PayChannel.of(request.getPayChannel());
    }

    private PayRecordPO findSuccessPayRecord(Long orderId) {
        return payRecordMapper.selectList(Wrappers.<PayRecordPO>lambdaQuery()
                        .eq(PayRecordPO::getOrderId, orderId)
                        .eq(PayRecordPO::getStatus, PayStatus.SUCCESS)
                        .orderByDesc(PayRecordPO::getPayTime)
                        .orderByDesc(PayRecordPO::getId))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private TradeOrderItemPO requireSingleItem(Long orderId) {
        TradeOrderItemPO item = tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemPO>lambdaQuery()
                        .eq(TradeOrderItemPO::getOrderId, orderId))
                .stream()
                .findFirst()
                .orElse(null);
        if (item == null) {
            throw new BizException(TradeErrorCode.TRADE_PAY_RECORD_INVALID, "订单明细不存在，不能支付");
        }
        return item;
    }

    private Long firstCourseId(TradeOrderPO order) {
        TradeOrderItemPO item = requireSingleItem(order.getId());
        return item.getCourseId();
    }

    private boolean hasJoinedCourse(Long userId, Long courseId) {
        return learningCourseMapper.selectCount(Wrappers.<LearningCoursePO>lambdaQuery()
                .eq(LearningCoursePO::getUserId, userId)
                .eq(LearningCoursePO::getCourseId, courseId)) > 0;
    }

    private PayResultVO paidResult(TradeOrderPO order, PayRecordPO payRecord, boolean learningGranted) {
        PayResultVO result = new PayResultVO();
        result.setOrderId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setPayNo(payRecord == null ? null : payRecord.getPayNo());
        result.setPayStatus(payRecord == null ? PayStatus.SUCCESS : payRecord.getStatus());
        result.setOrderStatus(order.getStatus());
        result.setPayAmount(order.getPayAmount());
        result.setPayTime(order.getPayTime());
        result.setLearningGranted(learningGranted);
        result.setMessage(learningGranted ? "支付成功，课程已加入学习" : "订单已支付，请稍后刷新学习课程");
        return result;
    }
}
