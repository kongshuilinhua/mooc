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
import com.elysia.mooc.course.domain.enums.CourseStatus;
import com.elysia.mooc.course.domain.po.CoursePO;
import com.elysia.mooc.course.mapper.CourseMapper;
import com.elysia.mooc.learning.mapper.LearningCourseMapper;
import com.elysia.mooc.trade.constants.TradeErrorCode;
import com.elysia.mooc.trade.domain.dto.CancelOrderRequest;
import com.elysia.mooc.trade.domain.dto.CreateOrderRequest;
import com.elysia.mooc.trade.domain.enums.OrderStatus;
import com.elysia.mooc.trade.domain.po.TradeOrderItemPO;
import com.elysia.mooc.trade.domain.po.TradeOrderPO;
import com.elysia.mooc.trade.domain.vo.OrderVO;
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

/** 订单服务核心业务测试。 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private LearningCourseMapper learningCourseMapper;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;

    @Mock
    private OrderNoGenerator orderNoGenerator;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldCreateUnpaidOrderForPaidCourse() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3002L)).thenReturn(course(3002L, new BigDecimal("199.00")));
        when(learningCourseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(tradeOrderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderNoGenerator.nextOrderNo()).thenReturn("ORD202605170001");
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCourseId(3002L);

        OrderVO result = orderService.createOrder(request);

        ArgumentCaptor<TradeOrderPO> orderCaptor = ArgumentCaptor.forClass(TradeOrderPO.class);
        verify(tradeOrderMapper).insert(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getOrderNo()).isEqualTo("ORD202605170001");
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.UNPAID);
        assertThat(orderCaptor.getValue().getPayAmount()).isEqualByComparingTo("199.00");
        verify(tradeOrderItemMapper).insert(any(TradeOrderItemPO.class));
        assertThat(result.getStatus()).isEqualTo(OrderStatus.UNPAID);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void createOrderShouldReuseExistingUnpaidOrder() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3002L)).thenReturn(course(3002L, new BigDecimal("199.00")));
        when(learningCourseMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        TradeOrderItemPO item = orderItem(21001L, 3002L);
        TradeOrderPO order = order(21001L, 3L, OrderStatus.UNPAID);
        when(tradeOrderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item), List.of(item));
        when(tradeOrderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCourseId(3002L);

        OrderVO result = orderService.createOrder(request);

        assertThat(result.getId()).isEqualTo(21001L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.UNPAID);
        verify(tradeOrderMapper, never()).insert(any(TradeOrderPO.class));
    }

    @Test
    void createOrderShouldRejectFreeCourse() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(courseMapper.selectById(3001L)).thenReturn(course(3001L, BigDecimal.ZERO));
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCourseId(3001L);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TradeErrorCode.TRADE_FREE_COURSE.code());
    }

    @Test
    void getOrderDetailShouldRejectOtherUserOrder() {
        when(userContextService.currentUserId()).thenReturn(4L);
        when(tradeOrderMapper.selectById(21001L)).thenReturn(order(21001L, 3L, OrderStatus.UNPAID));

        assertThatThrownBy(() -> orderService.getOrderDetail(21001L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TradeErrorCode.TRADE_ORDER_FORBIDDEN.code());
    }

    @Test
    void cancelOrderShouldRejectPaidOrder() {
        when(userContextService.currentUserId()).thenReturn(3L);
        when(tradeOrderMapper.selectById(21001L)).thenReturn(order(21001L, 3L, OrderStatus.PAID));

        assertThatThrownBy(() -> orderService.cancelOrder(21001L, new CancelOrderRequest()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(TradeErrorCode.TRADE_ORDER_STATUS_INVALID.code());
    }

    private CoursePO course(Long id, BigDecimal price) {
        CoursePO course = new CoursePO();
        course.setId(id);
        course.setTitle("测试课程");
        course.setCoverUrl("/cover.png");
        course.setPrice(price);
        course.setStatus(CourseStatus.PUBLISHED);
        return course;
    }

    private TradeOrderPO order(Long id, Long userId, OrderStatus status) {
        TradeOrderPO order = new TradeOrderPO();
        order.setId(id);
        order.setOrderNo("ORD202605170001");
        order.setUserId(userId);
        order.setTotalAmount(new BigDecimal("199.00"));
        order.setPayAmount(new BigDecimal("199.00"));
        order.setStatus(status);
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return order;
    }

    private TradeOrderItemPO orderItem(Long orderId, Long courseId) {
        TradeOrderItemPO item = new TradeOrderItemPO();
        item.setId(orderId + 1000);
        item.setOrderId(orderId);
        item.setCourseId(courseId);
        item.setCourseTitle("测试课程");
        item.setCourseCover("/cover.png");
        item.setPrice(new BigDecimal("199.00"));
        item.setQuantity(1);
        return item;
    }
}
