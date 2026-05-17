package com.elysia.mooc.trade.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elysia.mooc.auth.service.UserContextService;
import com.elysia.mooc.common.exception.BizException;
import com.elysia.mooc.event.domain.DomainEvent;
import com.elysia.mooc.event.service.BusinessEventPublisher;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 支付服务核心业务测试。 */
@ExtendWith(MockitoExtension.class)
class PayServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Mock
    private PayRecordMapper payRecordMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private OrderNoGenerator orderNoGenerator;

    @Mock
    private BusinessEventPublisher businessEventPublisher;

    @Mock
    private OrderPaidEventConsumer orderPaidEventConsumer;

    @InjectMocks
    private PayServiceImpl payService;

    @Test
    void mockPayShouldWritePayRecordPublishEventAndGrantLearning() {
        when(userContextService.currentUserId()).thenReturn(3L);
        TradeOrderPO order = order(OrderStatus.UNPAID);
        TradeOrderItemPO item = orderItem();
        when(tradeOrderMapper.selectById(21001L)).thenReturn(order);
        when(tradeOrderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(orderNoGenerator.nextPayNo()).thenReturn("PAY202605170001");
        DomainEvent event = DomainEvent.of("mooc.trade.order.paid", "mooc.trade.order.paid", "order:ORD1", "payload");
        when(businessEventPublisher.publishTradeOrderPaid(
                any(), any(), any(), any(), any(), any(), any())).thenReturn(event);
        when(orderPaidEventConsumer.consumeOrderPaid(event)).thenReturn(true);

        PayResultVO result = payService.mockPay(21001L, new MockPayRequest());

        ArgumentCaptor<PayRecordPO> payCaptor = ArgumentCaptor.forClass(PayRecordPO.class);
        verify(payRecordMapper).insert(payCaptor.capture());
        assertThat(payCaptor.getValue().getPayNo()).isEqualTo("PAY202605170001");
        assertThat(payCaptor.getValue().getPayChannel()).isEqualTo(PayChannel.MOCK);
        assertThat(payCaptor.getValue().getStatus()).isEqualTo(PayStatus.SUCCESS);
        verify(tradeOrderMapper).updateById(order);
        verify(businessEventPublisher).publishTradeOrderPaid(
                21001L,
                "ORD1",
                3L,
                3002L,
                new BigDecimal("199.00"),
                "PAY202605170001",
                order.getPayTime());
        verify(orderPaidEventConsumer).consumeOrderPaid(event);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(result.getLearningGranted()).isTrue();
    }

    @Test
    void mockPayShouldReturnIdempotentResultWhenAlreadyPaid() {
        when(userContextService.currentUserId()).thenReturn(3L);
        TradeOrderPO order = order(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        when(tradeOrderMapper.selectById(21001L)).thenReturn(order);
        when(payRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(payRecord()));
        when(tradeOrderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(orderItem()));
        when(learningCourseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        PayResultVO result = payService.mockPay(21001L, new MockPayRequest());

        assertThat(result.getPayNo()).isEqualTo("PAY1");
        assertThat(result.getLearningGranted()).isTrue();
        verify(payRecordMapper, never()).insert(any(PayRecordPO.class));
        verify(businessEventPublisher, never()).publishTradeOrderPaid(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void mockPayShouldRejectOtherUserOrder() {
        when(userContextService.currentUserId()).thenReturn(4L);
        when(tradeOrderMapper.selectById(21001L)).thenReturn(order(OrderStatus.UNPAID));

        assertThatThrownBy(() -> payService.mockPay(21001L, new MockPayRequest()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TradeErrorCode.TRADE_ORDER_FORBIDDEN.code());
    }

    private TradeOrderPO order(OrderStatus status) {
        TradeOrderPO order = new TradeOrderPO();
        order.setId(21001L);
        order.setOrderNo("ORD1");
        order.setUserId(3L);
        order.setTotalAmount(new BigDecimal("199.00"));
        order.setPayAmount(new BigDecimal("199.00"));
        order.setStatus(status);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return order;
    }

    private TradeOrderItemPO orderItem() {
        TradeOrderItemPO item = new TradeOrderItemPO();
        item.setId(21101L);
        item.setOrderId(21001L);
        item.setCourseId(3002L);
        item.setCourseTitle("测试课程");
        item.setPrice(new BigDecimal("199.00"));
        item.setQuantity(1);
        return item;
    }

    private PayRecordPO payRecord() {
        PayRecordPO payRecord = new PayRecordPO();
        payRecord.setPayNo("PAY1");
        payRecord.setStatus(PayStatus.SUCCESS);
        payRecord.setPayTime(LocalDateTime.now());
        return payRecord;
    }
}
